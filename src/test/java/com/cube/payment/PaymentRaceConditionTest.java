package com.cube.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.cube.support.TestSeed;

import com.cube.member.MemberRepository;
import com.cube.member.entity.Member;
import com.cube.member.entity.MemberGrade;
import com.cube.order.OrderRepository;
import com.cube.order.entity.Order;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentRaceConditionTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MemberRepository memberRepository;

    @SpyBean
    PaymentRepository paymentRepository;

    @SpyBean
    PaymentProcessor paymentProcessor;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM payment_discount");
        jdbcTemplate.update("DELETE FROM payment_detail");
        jdbcTemplate.update("DELETE FROM payment");
        jdbcTemplate.update("DELETE FROM orders");
        TestSeed.MEMBER_POINTS.forEach((grade, point) ->
                jdbcTemplate.update(
                        "UPDATE member SET point = ?, version = 0 WHERE grade = ?",
                        point, grade.name()));
        Mockito.reset(paymentRepository, paymentProcessor);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<String> jsonRequest(String body, String idempotencyKey) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Idempotency-Key", idempotencyKey);
        return new HttpEntity<>(body, h);
    }

    private Member memberOfGrade(MemberGrade grade) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getGrade() == grade)
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("동일 Idempotency-Key 로 N개 동시 POST → 모두 성공 응답, DB 결제는 단 한 건, catch 분기 진입 검증")
    void concurrentSameKeyResultsInSinglePayment() throws Exception {
        Member vip = memberOfGrade(MemberGrade.VIP);
        Order order = orderRepository.save(Order.create("동시상품", 10000, vip.getId()));
        String key = UUID.randomUUID().toString();

        String body = """
                {
                  "orderId": %d,
                  "means": [{"method":"CREDIT_CARD","amount":9000}]
                }
                """.formatted(order.getId());

        int threads = 8;

        CountDownLatch processGate = new CountDownLatch(threads);
        doAnswer(inv -> {
                    processGate.countDown();
                    processGate.await(2, TimeUnit.SECONDS);
                    return inv.callRealMethod();
                })
                .when(paymentProcessor)
                .process(eq(key), any(), any(), any());

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger nonOk = new AtomicInteger();
        AtomicReference<Long> samplePaymentId = new AtomicReference<>();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ResponseEntity<Map<String, Object>> r = rest.exchange(
                            url("/api/v1/payments"), HttpMethod.POST, jsonRequest(body, key), MAP_TYPE);
                    if (r.getStatusCode().is2xxSuccessful()) {
                        ok.incrementAndGet();
                        samplePaymentId.compareAndSet(null, ((Number) r.getBody().get("paymentId")).longValue());
                    } else {
                        nonOk.incrementAndGet();
                    }
                } catch (Exception ignored) {
                    nonOk.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertThat(done.await(20, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        assertThat(ok.get()).isEqualTo(threads);
        assertThat(paymentRepository.findByIdempotencyKey(key)).isPresent();
        long countWithKey = paymentRepository.findAll().stream()
                .filter(p -> key.equals(p.getIdempotencyKey()))
                .count();
        assertThat(countWithKey).isEqualTo(1L);
        assertThat(samplePaymentId.get()).isNotNull();

        verify(paymentProcessor, atLeast(threads)).process(eq(key), any(), any(), any());
        verify(paymentRepository, atLeast(threads + 1)).findByIdempotencyKey(key);
    }

    @Test
    @DisplayName("같은 회원에 동시 포인트 결제 → CAS 가 직렬화하여 포인트 합계가 정확히 차감된다")
    void concurrentPointDeductionsSerialize() throws Exception {
        Member vip = memberOfGrade(MemberGrade.VIP);
        long pointBefore = vip.getPoint();

        int threads = 5;
        long perPay = 1000L;
        long perOrderOriginal = perPay + 1000L;

        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            orderIds.add(orderRepository.save(Order.create("동시포인트", perOrderOriginal, vip.getId())).getId());
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger ok = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final Long orderId = orderIds.get(i);
            pool.submit(() -> {
                try {
                    start.await();
                    String body = """
                            {
                              "orderId": %d,
                              "means": [{"method":"POINT","amount":%d}]
                            }
                            """.formatted(orderId, perPay);
                    ResponseEntity<Map<String, Object>> r = rest.exchange(
                            url("/api/v1/payments"),
                            HttpMethod.POST,
                            jsonRequest(body, UUID.randomUUID().toString()),
                            MAP_TYPE);
                    if (r.getStatusCode().is2xxSuccessful()) {
                        ok.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        long expectedDeducted = ok.get() * 950L;
        Member after = memberRepository.findById(vip.getId()).orElseThrow();
        assertThat(after.getPoint()).isEqualTo(pointBefore - expectedDeducted);
        assertThat(ok.get()).isEqualTo(threads);
    }
}
