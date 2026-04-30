package com.cube.payment.discount;

import com.cube.payment.member.domain.Member;
import com.cube.payment.member.domain.MemberGrade;
import com.cube.payment.order.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("할인 정책 단위 테스트")
class DiscountPolicyTest {

    private final NormalDiscountPolicy normalPolicy = new NormalDiscountPolicy();
    private final VipDiscountPolicy vipPolicy = new VipDiscountPolicy();
    private final VvipDiscountPolicy vvipPolicy = new VvipDiscountPolicy();

    @Test
    @DisplayName("NORMAL 등급은 할인이 적용되지 않는다")
    void normal_member_no_discount() {
        Member member = new Member("일반회원", MemberGrade.NORMAL);
        Order order = new Order("상품A", 10_000L, member);

        long discount = normalPolicy.calculateDiscountAmount(order);

        assertThat(discount).isZero();
    }

    @Test
    @DisplayName("VIP 등급은 1,000원 고정 할인이 적용된다")
    void vip_member_fixed_1000_discount() {
        Member member = new Member("VIP회원", MemberGrade.VIP);
        Order order = new Order("상품B", 10_000L, member);

        long discount = vipPolicy.calculateDiscountAmount(order);

        assertThat(discount).isEqualTo(1_000L);
    }

    @Test
    @DisplayName("VIP 등급에서 주문 금액이 할인 금액보다 작으면 원가만큼만 할인된다")
    void vip_member_discount_capped_at_original_price() {
        Member member = new Member("VIP회원", MemberGrade.VIP);
        Order order = new Order("저가상품", 500L, member);

        long discount = vipPolicy.calculateDiscountAmount(order);

        assertThat(discount).isEqualTo(500L);
    }

    @Test
    @DisplayName("VVIP 등급은 주문 금액의 10%가 할인된다")
    void vvip_member_rate_10_percent_discount() {
        Member member = new Member("VVIP회원", MemberGrade.VVIP);
        Order order = new Order("상품C", 20_000L, member);

        long discount = vvipPolicy.calculateDiscountAmount(order);

        assertThat(discount).isEqualTo(2_000L);
    }

    @Test
    @DisplayName("NORMAL 회원의 최종 결제 금액은 원가와 동일하다")
    void normal_member_final_amount_equals_original_price() {
        Member member = new Member("일반회원", MemberGrade.NORMAL);
        Order order = new Order("상품A", 10_000L, member);

        long discount = normalPolicy.calculateDiscountAmount(order);
        long finalAmount = order.getOriginalPrice() - discount;

        assertThat(finalAmount).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("VIP 회원의 최종 결제 금액은 원가에서 1,000원을 뺀 금액이다")
    void vip_member_final_amount_is_original_minus_1000() {
        Member member = new Member("VIP회원", MemberGrade.VIP);
        Order order = new Order("상품B", 10_000L, member);

        long discount = vipPolicy.calculateDiscountAmount(order);
        long finalAmount = order.getOriginalPrice() - discount;

        assertThat(finalAmount).isEqualTo(9_000L);
    }

    @Test
    @DisplayName("VVIP 회원의 최종 결제 금액은 원가의 90%이다")
    void vvip_member_final_amount_is_90_percent_of_original() {
        Member member = new Member("VVIP회원", MemberGrade.VVIP);
        Order order = new Order("상품C", 20_000L, member);

        long discount = vvipPolicy.calculateDiscountAmount(order);
        long finalAmount = order.getOriginalPrice() - discount;

        assertThat(finalAmount).isEqualTo(18_000L);
    }
}
