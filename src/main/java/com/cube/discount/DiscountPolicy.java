package com.cube.discount;

@FunctionalInterface
public interface DiscountPolicy {
    long calculate(long originalPrice);
}
