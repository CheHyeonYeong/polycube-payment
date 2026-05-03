package com.cube.discount.strategy;

@FunctionalInterface
public interface DiscountStrategy {
    DiscountResult apply(DiscountContext context);
}
