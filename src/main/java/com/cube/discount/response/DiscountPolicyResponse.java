package com.cube.discount.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.cube.common.PaymentMethod;
import com.cube.discount.entity.DiscountPolicy;
import com.cube.discount.entity.DiscountType;
import com.cube.member.entity.MemberGrade;

public record DiscountPolicyResponse(
        Long id,
        String name,
        MemberGrade targetGrade,
        PaymentMethod targetPaymentMethod,
        DiscountType discountType,
        BigDecimal discountValue,
        int priority,
        boolean active,
        Long version,
        Instant updatedAt
) {
    public static DiscountPolicyResponse from(DiscountPolicy policy) {
        return new DiscountPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getTargetGrade(),
                policy.getTargetPaymentMethod(),
                policy.getDiscountType(),
                policy.getDiscountValue(),
                policy.getPriority(),
                policy.isActive(),
                policy.getVersion(),
                policy.getUpdatedAt());
    }
}
