package com.cube.payment.response;

import com.cube.common.PaymentMethod;
import com.cube.payment.entity.PaymentDetail;

public record PaymentDetailResponse(
        PaymentMethod method,
        long grossAmount,
        long chargedAmount
) {
    public static PaymentDetailResponse from(PaymentDetail detail) {
        return new PaymentDetailResponse(
                detail.getPaymentMethod(),
                detail.getGrossAmount(),
                detail.getChargedAmount()
        );
    }
}
