package com.cube.payment.payment.response;

import com.cube.payment.payment.entity.Payment;
import com.cube.payment.payment.entity.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentResponse {

    private final Long paymentId;
    private final Long orderId;
    private final String productName;
    private final long originalPrice;
    private final long discountAmount;
    private final long finalAmount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime paidAt;

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getOrder().getProductName(),
                payment.getOrder().getOriginalPrice(),
                payment.getDiscountAmount(),
                payment.getFinalAmount(),
                payment.getPaymentMethod(),
                payment.getPaidAt()
        );
    }
}
