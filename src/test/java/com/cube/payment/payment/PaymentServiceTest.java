package com.cube.payment.payment;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.member.MemberRepository;
import com.cube.payment.member.entity.Member;
import com.cube.payment.member.entity.MemberGrade;
import com.cube.payment.order.OrderRepository;
import com.cube.payment.order.entity.Order;
import com.cube.payment.payment.entity.PaymentMethod;
import com.cube.payment.payment.request.PaymentCreateRequest;
import com.cube.payment.payment.response.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired PaymentService paymentService;
    @Autowired MemberRepository memberRepository;
    @Autowired OrderRepository orderRepository;

    private Member normalMember;
    private Member vipMember;
    private Member vvipMember;

    @BeforeEach
    void setUp() {
        normalMember = memberRepository.save(new Member("일반회원", MemberGrade.NORMAL));
        vipMember = memberRepository.save(new Member("VIP회원", MemberGrade.VIP));
        vvipMember = memberRepository.save(new Member("VVIP회원", MemberGrade.VVIP));
    }

    @Test
    @DisplayName("NORMAL 회원이 신용카드로 결제하면 할인 없이 원가로 결제된다")
    void NORMAL_신용카드_할인없이_원가결제() {
        Order order = orderRepository.save(new Order("상품A", 10_000L, normalMember));

        PaymentResponse response = paymentService.pay(createRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        assertThat(response.getDiscountAmount()).isZero();
        assertThat(response.getFinalAmount()).isEqualTo(10_000L);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(response.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("NORMAL 회원이 포인트로 결제하면 할인 없이 원가로 결제된다")
    void NORMAL_포인트_할인없이_원가결제() {
        Order order = orderRepository.save(new Order("상품B", 10_000L, normalMember));

        PaymentResponse response = paymentService.pay(createRequest(order.getId(), PaymentMethod.POINT));

        assertThat(response.getDiscountAmount()).isZero();
        assertThat(response.getFinalAmount()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("VIP 회원이 신용카드로 결제하면 1,000원이 할인된다")
    void VIP_신용카드_1000원_할인() {
        Order order = orderRepository.save(new Order("상품C", 10_000L, vipMember));

        PaymentResponse response = paymentService.pay(createRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        assertThat(response.getDiscountAmount()).isEqualTo(1_000L);
        assertThat(response.getFinalAmount()).isEqualTo(9_000L);
    }

    @Test
    @DisplayName("VIP 회원이 포인트로 결제해도 등급 할인 1,000원만 적용된다")
    void VIP_포인트_등급할인만_적용() {
        Order order = orderRepository.save(new Order("상품D", 10_000L, vipMember));

        PaymentResponse response = paymentService.pay(createRequest(order.getId(), PaymentMethod.POINT));

        assertThat(response.getDiscountAmount()).isEqualTo(1_000L);
        assertThat(response.getFinalAmount()).isEqualTo(9_000L);
    }

    @Test
    @DisplayName("VVIP 회원이 신용카드로 결제하면 주문 금액의 10%가 할인된다")
    void VVIP_신용카드_10퍼센트_할인() {
        Order order = orderRepository.save(new Order("상품E", 20_000L, vvipMember));

        PaymentResponse response = paymentService.pay(createRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        assertThat(response.getDiscountAmount()).isEqualTo(2_000L);
        assertThat(response.getFinalAmount()).isEqualTo(18_000L);
    }

    @Test
    @DisplayName("결제 성공 시 주문 정보와 결제 일시가 저장된다")
    void 결제_성공시_저장_정보_검증() {
        Order order = orderRepository.save(new Order("상품F", 15_000L, vipMember));

        PaymentResponse response = paymentService.pay(createRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        assertThat(response.getPaymentId()).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(order.getId());
        assertThat(response.getProductName()).isEqualTo("상품F");
        assertThat(response.getOriginalPrice()).isEqualTo(15_000L);
        assertThat(response.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 ID로 결제 정보를 조회할 수 있다")
    void 결제_단건_조회_성공() {
        Order order = orderRepository.save(new Order("상품G", 10_000L, vipMember));
        PaymentResponse saved = paymentService.pay(createRequest(order.getId(), PaymentMethod.CREDIT_CARD));

        PaymentResponse found = paymentService.findById(saved.getPaymentId());

        assertThat(found.getPaymentId()).isEqualTo(saved.getPaymentId());
        assertThat(found.getFinalAmount()).isEqualTo(9_000L);
    }

    @Test
    @DisplayName("존재하지 않는 결제 ID 조회 시 PAYMENT_NOT_FOUND 예외가 발생한다")
    void 존재하지_않는_결제_조회_예외() {
        assertThatThrownBy(() -> paymentService.findById(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제하면 ORDER_NOT_FOUND 예외가 발생한다")
    void 존재하지_않는_주문_결제_예외() {
        assertThatThrownBy(() -> paymentService.pay(createRequest(999L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    private PaymentCreateRequest createRequest(Long orderId, PaymentMethod method) {
        PaymentCreateRequest request = new PaymentCreateRequest();
        setField(request, "orderId", orderId);
        setField(request, "paymentMethod", method);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
