package com.cube.payment.response;

import java.time.Instant;

import com.cube.common.PaymentMethod;
import com.cube.payment.entity.Payment;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        long finalAmount,
        PaymentMethod paymentMethod,
        Instant paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getFinalAmount(),
                payment.getPaymentMethod(),
                payment.getPaidAt());
    }
}
