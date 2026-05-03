package com.cube.payment.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cube.common.PaymentMethod;

public record PaymentMeansRequest(
        @NotNull(message = "결제수단은 필수입니다.")
        PaymentMethod method,
        @PositiveOrZero(message = "결제 금액은 0 이상이어야 합니다.")
        long amount
) {
}
