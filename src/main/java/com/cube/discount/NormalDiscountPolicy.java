package com.cube.discount;

import org.springframework.stereotype.Component;

@Component
public class NormalDiscountPolicy implements DiscountPolicy {

    @Override
    public long calculate(long originalPrice) {
        return 0L;
    }
}
