package com.cube.payment.response;

import java.math.BigDecimal;

import com.cube.common.PaymentMethod;
import com.cube.discount.entity.DiscountType;
import com.cube.payment.entity.AppliedScope;
import com.cube.payment.entity.PaymentDiscount;

public record AppliedDiscountResponse(
        String policyName,
        DiscountType discountType,
        BigDecimal discountValue,
        long discountAmount,
        AppliedScope appliedScope,
        PaymentMethod appliedMethod
) {
    public static AppliedDiscountResponse from(PaymentDiscount discount) {
        return new AppliedDiscountResponse(
                discount.getPolicyName(),
                discount.getDiscountType(),
                discount.getDiscountValue(),
                discount.getDiscountAmount(),
                discount.getAppliedScope(),
                discount.getAppliedMethod());
    }
}
