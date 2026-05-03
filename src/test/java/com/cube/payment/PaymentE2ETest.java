package com.cube.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import com.cube.member.MemberRepository;
import com.cube.member.entity.Member;
import com.cube.member.entity.MemberGrade;
import com.cube.order.OrderRepository;
import com.cube.order.entity.Order;
import com.cube.support.TestSeed;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentE2ETest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MemberRepository memberRepository;

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
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<String> jsonRequest(String body, String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (idempotencyKey != null) {
            headers.set("Idempotency-Key", idempotencyKey);
        }
        return new HttpEntity<>(body, headers);
    }

    private Member memberOfGrade(MemberGrade grade) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getGrade() == grade)
                .findFirst()
                .orElseThrow();
    }

    private Order createOrder(Member member, long price) {
        return orderRepository.save(Order.create("E2E상품", price, member.getId()));
    }

    @Test
    @DisplayName("E2E: VVIP 하이브리드 결제 후 GET 으로 동일 결과 조회")
    void hybridPaymentRoundTrip() {
        Member vvip = memberOfGrade(MemberGrade.VVIP);
        Order order = createOrder(vvip, 10000);
        String key = UUID.randomUUID().toString();

        String body = """
                {
                  "orderId": %d,
                  "means": [
                    {"method":"POINT","amount":4000},
                    {"method":"CREDIT_CARD","amount":5000}
                  ]
                }
                """.formatted(order.getId());

        ResponseEntity<Map<String, Object>> created = restTemplate.exchange(
                url("/api/v1/payments"), HttpMethod.POST, jsonRequest(body, key), MAP_TYPE);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> payBody = created.getBody();
        assertThat(payBody).isNotNull();
        assertThat(((Number) payBody.get("originalAmount")).longValue()).isEqualTo(10000L);
        assertThat(((Number) payBody.get("discountAmount")).longValue()).isEqualTo(1200L);
        assertThat(((Number) payBody.get("finalAmount")).longValue()).isEqualTo(8800L);
        Long paymentId = ((Number) payBody.get("paymentId")).longValue();

        ResponseEntity<Map<String, Object>> read = restTemplate.exchange(
                url("/api/v1/payments/" + paymentId), HttpMethod.GET, jsonRequest(null, null), MAP_TYPE);
        assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) read.getBody().get("finalAmount")).longValue()).isEqualTo(8800L);
    }

    @Test
    @DisplayName("E2E: 같은 Idempotency-Key 로 재호출 → 첫 응답 201, 두 번째 200, 동일 paymentId")
    void idempotentRetryReturnsSamePayment() {
        Member vip = memberOfGrade(MemberGrade.VIP);
        Order order = createOrder(vip, 10000);
        String key = UUID.randomUUID().toString();

        String body = """
                {
                  "orderId": %d,
                  "means": [{"method":"CREDIT_CARD","amount":9000}]
                }
                """.formatted(order.getId());

        ResponseEntity<Map<String, Object>> first = restTemplate.exchange(
                url("/api/v1/payments"), HttpMethod.POST, jsonRequest(body, key), MAP_TYPE);
        ResponseEntity<Map<String, Object>> second = restTemplate.exchange(
                url("/api/v1/payments"), HttpMethod.POST, jsonRequest(body, key), MAP_TYPE);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody().get("paymentId")).isEqualTo(second.getBody().get("paymentId"));
    }

    @Test
    @DisplayName("E2E: 포인트 부족 시 400 + POINT_NOT_ENOUGH 코드와 페이로드(required/available) 반환")
    void insufficientPointReturnsAdtPayload() {
        Member normal = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(normal, 10000);
        String key = UUID.randomUUID().toString();

        String body = """
                {
                  "orderId": %d,
                  "means": [{"method":"POINT","amount":10000}]
                }
                """.formatted(order.getId());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url("/api/v1/payments"), HttpMethod.POST, jsonRequest(body, key), MAP_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> err = response.getBody();
        assertThat(err.get("code")).isEqualTo("POINT_NOT_ENOUGH");
        assertThat(((Number) err.get("required")).longValue()).isEqualTo(9500L);
        assertThat(((Number) err.get("available")).longValue()).isEqualTo(0L);
    }

    @Test
    @DisplayName("E2E: 빈 means 면 400 INVALID_REQUEST")
    void emptyMeansBadRequest() {
        Member vip = memberOfGrade(MemberGrade.VIP);
        Order order = createOrder(vip, 10000);

        String body = """
                {
                  "orderId": %d,
                  "means": []
                }
                """.formatted(order.getId());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url("/api/v1/payments"), HttpMethod.POST, jsonRequest(body, UUID.randomUUID().toString()), MAP_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("code")).isEqualTo("INVALID_REQUEST");
    }
}
