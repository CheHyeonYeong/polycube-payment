package com.cube.discount.strategy;

import java.math.BigDecimal;

import com.cube.common.Money;
import com.cube.discount.entity.DiscountType;

public record DiscountResult(
        Long policyId,
        String policyName,
        Long policyVersion,
        DiscountType type,
        BigDecimal value,
        Money appliedAmount
) {
}
