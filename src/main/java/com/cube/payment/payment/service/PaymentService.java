package com.cube.payment.payment.service;

import com.cube.payment.discount.DiscountPolicy;
import com.cube.payment.discount.DiscountPolicyProvider;
import com.cube.payment.order.domain.Order;
import com.cube.payment.order.repository.OrderRepository;
import com.cube.payment.payment.domain.Payment;
import com.cube.payment.payment.dto.PaymentRequest;
import com.cube.payment.payment.dto.PaymentResponse;
import com.cube.payment.payment.repository.PaymentRepository;
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
    public PaymentResponse pay(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + request.getOrderId()));

        DiscountPolicy policy = discountPolicyProvider.getPolicy(order.getMember().getGrade());
        long discountAmount = policy.calculateDiscountAmount(order);
        long finalAmount = order.getOriginalPrice() - discountAmount;

        Payment payment = new Payment(order, discountAmount, finalAmount,
                request.getPaymentMethod(), LocalDateTime.now());
        paymentRepository.save(payment);

        return new PaymentResponse(payment);
    }
}
