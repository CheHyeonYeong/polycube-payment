package com.cube.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cube.common.Money;
import com.cube.common.PaymentMethod;
import com.cube.discount.entity.DiscountPolicy;
import com.cube.discount.entity.DiscountType;
import com.cube.discount.strategy.DiscountContext;
import com.cube.discount.strategy.DiscountResult;
import com.cube.discount.strategy.DiscountStrategies;
import com.cube.member.entity.MemberGrade;

class DiscountStrategiesTest {

    static final class PolicyFixture {
        static DiscountPolicy vipFixed1000() {
            return DiscountPolicy.create("VIP_FIXED", MemberGrade.VIP, null,
                    DiscountType.FIXED, BigDecimal.valueOf(1000), 10);
        }

        static DiscountPolicy bigFixed50000() {
            return DiscountPolicy.create("BIG_FIXED", MemberGrade.VIP, null,
                    DiscountType.FIXED, BigDecimal.valueOf(50000), 10);
        }

        static DiscountPolicy vvipRate10() {
            return DiscountPolicy.create("VVIP_RATE", MemberGrade.VVIP, null,
                    DiscountType.RATE, new BigDecimal("0.10"), 10);
        }

        static DiscountPolicy pointRate5() {
            return DiscountPolicy.create("POINT_RATE", null, PaymentMethod.POINT,
                    DiscountType.RATE, new BigDecimal("0.05"), 20);
        }
    }

    @Test
    @DisplayName("FIXED 전략은 정해진 금액만큼 할인한다")
    void fixedAppliesFlatAmount() {
        DiscountResult result = DiscountStrategies.fixed(PolicyFixture.vipFixed1000())
                .apply(DiscountContext.of(Money.of(10000), MemberGrade.VIP));

        assertEquals(1000L, result.appliedAmount().amount());
        assertEquals(DiscountType.FIXED, result.type());
        assertEquals("VIP_FIXED", result.policyName());
    }

    @Test
    @DisplayName("FIXED 전략은 현재 가격보다 큰 할인을 막는다")
    void fixedIsCappedAtCurrentPrice() {
        DiscountResult result = DiscountStrategies.fixed(PolicyFixture.bigFixed50000())
                .apply(DiscountContext.of(Money.of(10000), MemberGrade.VIP));

        assertEquals(10000L, result.appliedAmount().amount());
    }

    @Test
    @DisplayName("RATE 전략은 비율 만큼 할인한다")
    void rateAppliesPercentage() {
        DiscountResult result = DiscountStrategies.rate(PolicyFixture.vvipRate10())
                .apply(DiscountContext.of(Money.of(10000), MemberGrade.VVIP));

        assertEquals(1000L, result.appliedAmount().amount());
        assertEquals(DiscountType.RATE, result.type());
    }

    @Test
    @DisplayName("RATE 전략은 결제수단 컨텍스트에서도 동일하게 동작한다")
    void rateAppliesOnPaymentPortion() {
        DiscountResult result = DiscountStrategies.rate(PolicyFixture.pointRate5())
                .apply(DiscountContext.of(Money.of(4000), MemberGrade.VIP)
                        .withPaymentMethod(PaymentMethod.POINT));

        assertEquals(200L, result.appliedAmount().amount());
    }

}
