package com.cube.discount.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DiscountPolicyUpdateRequest(
        @NotNull(message = "expectedVersion 은 필수입니다.")
        Long expectedVersion,
        @NotNull(message = "newValue 는 필수입니다.")
        @PositiveOrZero(message = "newValue 는 0 이상이어야 합니다.")
        BigDecimal newValue
) {
}
