package com.cube.payment.discount;

import com.cube.payment.member.entity.Member;
import com.cube.payment.member.entity.MemberGrade;
import com.cube.payment.order.entity.Order;
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
    void NORMAL_등급_할인_없음() {
        Member member = new Member("일반회원", MemberGrade.NORMAL);
        Order order = new Order("상품A", 10_000L, member);

        assertThat(normalPolicy.calculateDiscountAmount(order)).isZero();
    }

    @Test
    @DisplayName("VIP 등급은 1,000원 고정 할인이 적용된다")
    void VIP_등급_1000원_고정_할인() {
        Member member = new Member("VIP회원", MemberGrade.VIP);
        Order order = new Order("상품B", 10_000L, member);

        assertThat(vipPolicy.calculateDiscountAmount(order)).isEqualTo(1_000L);
    }

    @Test
    @DisplayName("VIP 등급에서 주문 금액이 1,000원 미만이면 원가만큼만 할인된다")
    void VIP_주문금액_소액시_원가만큼_할인() {
        Member member = new Member("VIP회원", MemberGrade.VIP);
        Order order = new Order("저가상품", 500L, member);

        assertThat(vipPolicy.calculateDiscountAmount(order)).isEqualTo(500L);
    }

    @Test
    @DisplayName("VVIP 등급은 주문 금액의 10%가 할인된다")
    void VVIP_등급_10퍼센트_할인() {
        Member member = new Member("VVIP회원", MemberGrade.VVIP);
        Order order = new Order("상품C", 20_000L, member);

        assertThat(vvipPolicy.calculateDiscountAmount(order)).isEqualTo(2_000L);
    }

    @Test
    @DisplayName("NORMAL 회원의 최종 결제 금액은 원가와 동일하다")
    void NORMAL_최종금액_원가와_동일() {
        Member member = new Member("일반회원", MemberGrade.NORMAL);
        Order order = new Order("상품A", 10_000L, member);

        long finalAmount = order.getOriginalPrice() - normalPolicy.calculateDiscountAmount(order);

        assertThat(finalAmount).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("VIP 회원의 최종 결제 금액은 원가에서 1,000원을 뺀 금액이다")
    void VIP_최종금액_원가에서_1000원_차감() {
        Member member = new Member("VIP회원", MemberGrade.VIP);
        Order order = new Order("상품B", 10_000L, member);

        long finalAmount = order.getOriginalPrice() - vipPolicy.calculateDiscountAmount(order);

        assertThat(finalAmount).isEqualTo(9_000L);
    }

    @Test
    @DisplayName("VVIP 회원의 최종 결제 금액은 원가의 90%이다")
    void VVIP_최종금액_원가의_90퍼센트() {
        Member member = new Member("VVIP회원", MemberGrade.VVIP);
        Order order = new Order("상품C", 20_000L, member);

        long finalAmount = order.getOriginalPrice() - vvipPolicy.calculateDiscountAmount(order);

        assertThat(finalAmount).isEqualTo(18_000L);
    }

    @Test
    @DisplayName("할인 정책명이 올바르게 반환된다")
    void 할인_정책명_올바르게_반환() {
        assertThat(normalPolicy.getPolicyName()).isEqualTo("NORMAL_NO_DISCOUNT");
        assertThat(vipPolicy.getPolicyName()).isEqualTo("VIP_FIXED_1000");
        assertThat(vvipPolicy.getPolicyName()).isEqualTo("VVIP_RATE_10_PERCENT");
    }
}
