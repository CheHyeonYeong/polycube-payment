package com.cube.discount.request;

import jakarta.validation.constraints.NotNull;

public record DiscountPolicyDeactivateRequest(
        @NotNull(message = "expectedVersion 은 필수입니다.")
        Long expectedVersion
) {
}
