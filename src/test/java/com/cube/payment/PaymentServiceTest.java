package com.cube.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.cube.payment.request.PaymentRequest;
import com.cube.payment.response.PaymentResponse;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentServiceTest {

    @Autowired PaymentService paymentService;
    @Autowired MemberRepository memberRepository;
    @Autowired OrderRepository orderRepository;

    private Member memberOfGrade(MemberGrade grade) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getGrade() == grade)
                .findFirst()
                .orElseThrow();
    }

    private Order createOrder(Member member, long price) {
        return orderRepository.save(Order.create("상품", price, member.getId()));
    }

    @Test
    @DisplayName("NORMAL 등급은 할인 없이 원가 그대로 결제된다")
    void NORMAL_등급은_할인없이_결제() {
        Member member = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(member, 10_000L);

        PaymentResponse response = paymentService.pay(
                new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD, 10_000L));

        assertEquals(10_000L, response.finalAmount());
    }

    @Test
    @DisplayName("VIP 등급은 1,000원 정액 할인 후 결제된다")
    void VIP_등급은_1000원_할인_후_결제() {
        Member member = memberOfGrade(MemberGrade.VIP);
        Order order = createOrder(member, 10_000L);

        PaymentResponse response = paymentService.pay(
                new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD, 9_000L));

        assertEquals(9_000L, response.finalAmount());
    }

    @Test
    @DisplayName("VVIP 등급은 10% 정률 할인 후 결제된다")
    void VVIP_등급은_10퍼센트_할인_후_결제() {
        Member member = memberOfGrade(MemberGrade.VVIP);
        Order order = createOrder(member, 10_000L);

        PaymentResponse response = paymentService.pay(
                new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD, 9_000L));

        assertEquals(9_000L, response.finalAmount());
    }

    @Test
    @DisplayName("포인트 결제 시 포인트가 차감된다")
    void 포인트_결제_시_포인트_차감() {
        Member member = memberOfGrade(MemberGrade.VIP);
        long pointBefore = member.getPoint();
        Order order = createOrder(member, 10_000L);

        paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.POINT, 9_000L));

        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertEquals(pointBefore - 9_000L, updated.getPoint());
    }

    @Test
    @DisplayName("포인트가 부족하면 결제에 실패한다")
    void 포인트_부족시_결제_실패() {
        Member member = memberOfGrade(MemberGrade.NORMAL);
        Order order = createOrder(member, 10_000L);

        CustomException ex = assertThrows(CustomException.class, () ->
                paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.POINT, 10_000L)));

        assertTrue(ex.getError() instanceof DomainError.InsufficientPoints p
                && p.memberId().equals(member.getId()));
    }

    @Test
    @DisplayName("결제 금액이 할인 후 금액과 다르면 예외가 발생한다")
    void 결제금액_불일치시_예외() {
        Member member = memberOfGrade(MemberGrade.VIP);
        Order order = createOrder(member, 10_000L);

        CustomException ex = assertThrows(CustomException.class, () ->
                paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD, 8_000L)));

        assertTrue(ex.getError() instanceof DomainError.PaymentAmountMismatch m
                && m.expected() == 9_000L && m.actual() == 8_000L);
    }
}
