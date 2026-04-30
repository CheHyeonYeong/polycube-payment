package com.cube.payment.payment.dto;

import com.cube.payment.payment.domain.Payment;
import com.cube.payment.payment.domain.PaymentMethod;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentResponse {
    private final Long paymentId;
    private final Long orderId;
    private final String productName;
    private final long originalPrice;
    private final long discountAmount;
    private final long finalAmount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime paidAt;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getId();
        this.orderId = payment.getOrder().getId();
        this.productName = payment.getOrder().getProductName();
        this.originalPrice = payment.getOrder().getOriginalPrice();
        this.discountAmount = payment.getDiscountAmount();
        this.finalAmount = payment.getFinalAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.paidAt = payment.getPaidAt();
    }
}
