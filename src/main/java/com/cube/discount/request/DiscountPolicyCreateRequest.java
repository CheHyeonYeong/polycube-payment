package com.cube.discount.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cube.common.PaymentMethod;
import com.cube.discount.entity.DiscountType;
import com.cube.member.entity.MemberGrade;

public record DiscountPolicyCreateRequest(
        @NotBlank(message = "정책 이름은 비어 있을 수 없습니다.")
        String name,
        MemberGrade targetGrade,
        PaymentMethod targetPaymentMethod,
        @NotNull(message = "할인 타입은 필수입니다.")
        DiscountType discountType,
        @NotNull(message = "할인 값은 필수입니다.")
        @PositiveOrZero(message = "할인 값은 0 이상이어야 합니다.")
        BigDecimal discountValue,
        int priority
) {
    @AssertTrue(message = "discountType 에 적합하지 않은 discountValue 입니다.")
    public boolean isDiscountValueValidForType() {
        return discountType != null && discountValue != null
                && discountType.isValid(discountValue);
    }
}
