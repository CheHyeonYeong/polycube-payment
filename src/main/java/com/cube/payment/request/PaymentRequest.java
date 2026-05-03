package com.cube.payment.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "주문 식별자는 필수입니다.")
        Long orderId,
        @NotEmpty(message = "결제수단은 최소 1개 이상이어야 합니다.")
        @Valid List<PaymentMeansRequest> means
) {
}
