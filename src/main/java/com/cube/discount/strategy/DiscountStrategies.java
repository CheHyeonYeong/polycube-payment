package com.cube.discount.strategy;

import com.cube.common.Money;
import com.cube.discount.entity.DiscountPolicy;
import com.cube.discount.entity.DiscountType;

/** invariant 가 보장된 정책을 가정. 검증은 DiscountPolicy.toStrategy 가 책임. */
public final class DiscountStrategies {

    private DiscountStrategies() {
    }

    public static DiscountStrategy fixed(DiscountPolicy policy) {
        return ctx -> {
            Money cap = Money.of(policy.getDiscountValue().longValueExact());
            Money applied = cap.min(ctx.currentPrice());
            return new DiscountResult(
                    policy.getId(),
                    policy.getName(),
                    policy.getVersion(),
                    DiscountType.FIXED,
                    policy.getDiscountValue(),
                    applied);
        };
    }

    public static DiscountStrategy rate(DiscountPolicy policy) {
        return ctx -> {
            // RATE 의 0~1 invariant 가 진입 시점에 보장되므로 cap 은 다중 정책 누적 안전망 차원으로 유지.
            Money applied = ctx.currentPrice().multiply(policy.getDiscountValue()).min(ctx.currentPrice());
            return new DiscountResult(
                    policy.getId(),
                    policy.getName(),
                    policy.getVersion(),
                    DiscountType.RATE,
                    policy.getDiscountValue(),
                    applied);
        };
    }
}
