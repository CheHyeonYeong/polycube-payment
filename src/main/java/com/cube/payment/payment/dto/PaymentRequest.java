package com.cube.payment.payment.dto;

import com.cube.payment.payment.domain.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentRequest {
    private final Long orderId;
    private final PaymentMethod paymentMethod;
}
