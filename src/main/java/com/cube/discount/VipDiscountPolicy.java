package com.cube.discount;

import org.springframework.stereotype.Component;

@Component
public class VipDiscountPolicy implements DiscountPolicy {

    private static final long FIXED_DISCOUNT = 1_000L;

    @Override
    public long calculate(long originalPrice) {
        return Math.min(FIXED_DISCOUNT, originalPrice);
    }
}
