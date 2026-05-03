package com.cube.discount.strategy;

import java.util.List;

import com.cube.common.Money;

public record DiscountSummary(
        List<DiscountResult> results,
        Money totalDiscount,
        Money finalPrice
) {
    public DiscountSummary {
        results = List.copyOf(results);
    }
}
