package com.cube.payment;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.member.MemberRepository;
import com.cube.member.entity.Member;
import com.cube.member.entity.MemberGrade;
import com.cube.order.OrderRepository;
import com.cube.order.entity.Order;
import com.cube.order.entity.OrderStatus;
import com.cube.payment.PaymentService.PayResult;
import com.cube.payment.entity.AppliedScope;
import com.cube.payment.request.PaymentMeansRequest;
import com.cube.payment.request.PaymentRequest;
import com.cube.payment.response.AppliedDiscountResponse;
import com.cube.payment.response.PaymentResponse;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MemberRepository memberRepository;

    private Member memberOfGrade(MemberGrade grade) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getGrade() == grade)
                .findFirst()
                .orElseThrow();
    }

    private Order createOrder(Member member, long price) {
        return orderRepository.save(Order.create("상품", price, member.getId()));
    }

    private static PaymentMeansRequest means(PaymentMethod method, long amount) {
        return new PaymentMeansRequest(method, amount);
    }

    private static String newKey() {
        return UUID.randomUUID().toString();
    }

    private PaymentResponse pay(Long orderId, List<PaymentMeansRequest> means) {
        return paymentService.pay(newKey(), new PaymentRequest(orderId, means)).response();
    }

    @Test
    @DisplayName("NORMAL 등급은 할인 없이 그대로 결제한다")
    void normalGradeNoDiscount() {
        Member member = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(member, 10000);

        PaymentResponse response = pay(order.getId(), List.of(means(PaymentMethod.CREDIT_CARD, 10000)));

        assertAll(
                () -> assertEquals(10000L, response.originalAmount()),
                () -> assertEquals(0L, response.discountAmount()),
                () -> assertEquals(10000L, response.finalAmount()));
    }

    @Test
    @DisplayName("VIP 등급은 1,000원 정액 할인 후 카드 결제 9,000원")
    void vipFixedDiscount() {
        Member member = memberOfGrade(MemberGrade.VIP);
        Order order = createOrder(member, 10000);

        PaymentResponse response = pay(order.getId(), List.of(means(PaymentMethod.CREDIT_CARD, 9000)));

        assertAll(
                () -> assertEquals(1000L, response.discountAmount()),
                () -> assertEquals(9000L, response.finalAmount()));
    }

    @Test
    @DisplayName("VVIP 등급은 10% 정률 할인 후 카드 결제 9,000원")
    void vvipRateDiscount() {
        Member member = memberOfGrade(MemberGrade.VVIP);
        Order order = createOrder(member, 10000);

        PaymentResponse response = pay(order.getId(), List.of(means(PaymentMethod.CREDIT_CARD, 9000)));

        assertEquals(9000L, response.finalAmount());
    }

    @Test
    @DisplayName("VIP가 포인트로만 결제하면 등급 할인 + 포인트 5% 중복 할인")
    void vipPointPaymentStacksDiscount() {
        Member member = memberOfGrade(MemberGrade.VIP);
        long pointBefore = member.getPoint();
        Order order = createOrder(member, 10000);

        PaymentResponse response = pay(order.getId(), List.of(means(PaymentMethod.POINT, 9000)));

        assertEquals(8550L, response.finalAmount());
        assertEquals(1450L, response.discountAmount());

        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertEquals(pointBefore - 8550L, updated.getPoint());
    }

    @Test
    @DisplayName("VVIP가 카드+포인트 하이브리드 결제 시 포인트 portion 에만 5% 추가 할인")
    void vvipHybridPayment() {
        Member member = memberOfGrade(MemberGrade.VVIP);
        Order order = createOrder(member, 10000);

        PaymentResponse response = pay(order.getId(), List.of(
                means(PaymentMethod.POINT, 4000),
                means(PaymentMethod.CREDIT_CARD, 5000)));

        assertAll(
                () -> assertEquals(8800L, response.finalAmount()),
                () -> assertEquals(1200L, response.discountAmount()),
                () -> assertEquals(2, response.details().size()));

        List<AppliedDiscountResponse> applied = response.appliedDiscounts();
        assertTrue(applied.stream().anyMatch(d -> d.appliedScope() == AppliedScope.GRADE));
        assertTrue(applied.stream().anyMatch(d ->
                d.appliedScope() == AppliedScope.PAYMENT_METHOD
                        && d.appliedMethod() == PaymentMethod.POINT));
    }

    @Test
    @DisplayName("means 합이 등급 할인 후 금액과 다르면 예외")
    void meansSumMismatchThrows() {
        Member member = memberOfGrade(MemberGrade.VIP);
        Order order = createOrder(member, 10000);

        CustomException ex = assertThrows(CustomException.class,
                () -> pay(order.getId(), List.of(means(PaymentMethod.CREDIT_CARD, 8000))));
        assertTrue(ex.getError() instanceof DomainError.PaymentAmountMismatch m
                && m.expected() == 9000L && m.actual() == 8000L);
    }

    @Test
    @DisplayName("포인트가 부족하면 결제 실패")
    void insufficientPointsThrows() {
        Member member = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(member, 10000);

        CustomException ex = assertThrows(CustomException.class,
                () -> pay(order.getId(), List.of(means(PaymentMethod.POINT, 10000))));
        assertTrue(ex.getError() instanceof DomainError.PointNotEnough p
                && p.memberId().equals(member.getId())
                && p.required() == 9500L
                && p.available() == 0L);
    }

    @Test
    @DisplayName("같은 멱등성 키로 재호출하면 기존 결제 결과를 그대로 반환하고 포인트는 한 번만 차감된다")
    void duplicateIdempotencyKeyReturnsOriginal() {
        Member member = memberOfGrade(MemberGrade.VIP);
        long pointBefore = member.getPoint();
        Order order = createOrder(member, 10000);

        String key = newKey();
        PaymentRequest request = new PaymentRequest(order.getId(), List.of(means(PaymentMethod.POINT, 9000)));

        PayResult first = paymentService.pay(key, request);
        PayResult retried = paymentService.pay(key, request);

        assertEquals(first.response().paymentId(), retried.response().paymentId());
        assertEquals(first.response().finalAmount(), retried.response().finalAmount());
        assertTrue(first.newlyCreated());
        assertTrue(!retried.newlyCreated());

        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertEquals(pointBefore - 8550L, updated.getPoint());
    }

    @Test
    @DisplayName("결제 성공 시 주문 상태가 PAID 로 전이된다 (더티체킹 검증)")
    void orderTransitsToPaid() {
        Member member = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(member, 10000);
        Long orderId = order.getId();

        pay(orderId, List.of(means(PaymentMethod.CREDIT_CARD, 10000)));

        Order reloaded = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.PAID, reloaded.getStatus());
    }

    @Test
    @DisplayName("0원 주문은 means 합 0과 일치하면 결제 성공")
    void zeroAmountOrder() {
        Member member = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(member, 0);

        PaymentResponse response = pay(order.getId(), List.of(means(PaymentMethod.CREDIT_CARD, 0)));

        assertAll(
                () -> assertEquals(0L, response.originalAmount()),
                () -> assertEquals(0L, response.discountAmount()),
                () -> assertEquals(0L, response.finalAmount()));
    }

}
