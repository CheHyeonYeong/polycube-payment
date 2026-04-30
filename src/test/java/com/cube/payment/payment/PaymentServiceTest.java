package com.cube.payment.payment;

import com.cube.payment.member.domain.Member;
import com.cube.payment.member.domain.MemberGrade;
import com.cube.payment.member.repository.MemberRepository;
import com.cube.payment.order.domain.Order;
import com.cube.payment.order.repository.OrderRepository;
import com.cube.payment.payment.domain.PaymentMethod;
import com.cube.payment.payment.dto.PaymentRequest;
import com.cube.payment.payment.dto.PaymentResponse;
import com.cube.payment.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("결제 서비스 통합 테스트")
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("NORMAL 회원이 신용카드로 결제하면 할인 없이 원가로 결제된다")
    void normal_member_credit_card_payment() {
        Member member = memberRepository.save(new Member("일반회원", MemberGrade.NORMAL));
        Order order = orderRepository.save(new Order("상품A", 10_000L, member));

        PaymentResponse response = paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        assertThat(response.getDiscountAmount()).isZero();
        assertThat(response.getFinalAmount()).isEqualTo(10_000L);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(response.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("VIP 회원이 신용카드로 결제하면 1,000원이 할인된다")
    void vip_member_credit_card_payment() {
        Member member = memberRepository.save(new Member("VIP회원", MemberGrade.VIP));
        Order order = orderRepository.save(new Order("상품B", 10_000L, member));

        PaymentResponse response = paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        assertThat(response.getDiscountAmount()).isEqualTo(1_000L);
        assertThat(response.getFinalAmount()).isEqualTo(9_000L);
    }

    @Test
    @DisplayName("VVIP 회원이 포인트로 결제하면 10%가 할인된다")
    void vvip_member_point_payment() {
        Member member = memberRepository.save(new Member("VVIP회원", MemberGrade.VVIP));
        Order order = orderRepository.save(new Order("상품C", 20_000L, member));

        PaymentResponse response = paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.POINT));

        assertThat(response.getDiscountAmount()).isEqualTo(2_000L);
        assertThat(response.getFinalAmount()).isEqualTo(18_000L);
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제하면 예외가 발생한다")
    void payment_with_nonexistent_order_throws_exception() {
        assertThatThrownBy(() -> paymentService.pay(new PaymentRequest(999L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 주문");
    }
}
