package com.cube.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountPolicyTest {

    @Test
    @DisplayName("NORMAL 등급은 할인이 없다")
    void NORMAL은_할인이_없다() {
        DiscountPolicy policy = new NormalDiscountPolicy();
        assertEquals(0L, policy.calculate(10_000L));
    }

    @Test
    @DisplayName("VIP 등급은 1,000원 정액 할인을 받는다")
    void VIP는_1000원_정액_할인() {
        DiscountPolicy policy = new VipDiscountPolicy();
        assertEquals(1_000L, policy.calculate(10_000L));
    }

    @Test
    @DisplayName("VIP 할인이 주문 원가보다 크면 원가를 상한으로 한다")
    void VIP_할인이_원가보다_크면_원가까지만() {
        DiscountPolicy policy = new VipDiscountPolicy();
        assertEquals(500L, policy.calculate(500L));
    }

    @Test
    @DisplayName("VVIP 등급은 10% 정률 할인을 받는다")
    void VVIP는_10퍼센트_정률_할인() {
        DiscountPolicy policy = new VvipDiscountPolicy();
        assertEquals(1_000L, policy.calculate(10_000L));
    }
}
