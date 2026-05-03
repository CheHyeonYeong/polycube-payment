package com.cube.payment.response;

import java.time.Instant;
import java.util.List;

import com.cube.payment.entity.Payment;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        long originalAmount,
        long discountAmount,
        long finalAmount,
        Instant paidAt,
        List<PaymentDetailResponse> details,
        List<AppliedDiscountResponse> appliedDiscounts
) {
    public static PaymentResponse of(Payment payment,
                                     List<PaymentDetailResponse> details,
                                     List<AppliedDiscountResponse> appliedDiscounts) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getOriginalAmount(),
                payment.getDiscountAmount(),
                payment.getFinalAmount(),
                payment.getPaidAt(),
                List.copyOf(details),
                List.copyOf(appliedDiscounts));
    }
}
