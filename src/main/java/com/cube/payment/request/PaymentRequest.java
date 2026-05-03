package com.cube.payment.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.cube.common.PaymentMethod;

public record PaymentRequest(
        @NotNull(message = "주문 식별자는 필수입니다.")
        Long orderId,

        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentMethod paymentMethod,

        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        long amount
) {
}
