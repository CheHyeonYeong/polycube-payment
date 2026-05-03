package com.cube.discount;

import org.springframework.stereotype.Component;

@Component
public class VvipDiscountPolicy implements DiscountPolicy {

    @Override
    public long calculate(long originalPrice) {
        return originalPrice / 10;
    }
}
