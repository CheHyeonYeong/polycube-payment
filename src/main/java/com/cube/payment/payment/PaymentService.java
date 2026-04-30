package com.cube.payment.payment;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.discount.DiscountPolicy;
import com.cube.payment.discount.DiscountPolicyProvider;
import com.cube.payment.order.OrderRepository;
import com.cube.payment.order.entity.Order;
import com.cube.payment.payment.entity.Payment;
import com.cube.payment.payment.request.PaymentCreateRequest;
import com.cube.payment.payment.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountPolicyProvider discountPolicyProvider;

    @Transactional
    public PaymentResponse pay(PaymentCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        DiscountPolicy policy = discountPolicyProvider.getPolicy(order.getMember().getGrade());
        long discountAmount = policy.calculateDiscountAmount(order);
        long finalAmount = order.getOriginalPrice() - discountAmount;

        Payment payment = new Payment(order, discountAmount, finalAmount,
                request.getPaymentMethod(), LocalDateTime.now());
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    public PaymentResponse findById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}
