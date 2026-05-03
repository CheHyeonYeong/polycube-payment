package com.cube.payment;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.discount.DiscountPolicyProvider;
import com.cube.member.MemberService;
import com.cube.member.entity.Member;
import com.cube.order.OrderService;
import com.cube.order.entity.Order;
import com.cube.payment.entity.Payment;
import com.cube.payment.request.PaymentRequest;
import com.cube.payment.response.PaymentResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final MemberService memberService;
    private final DiscountPolicyProvider discountPolicyProvider;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        Order order = orderService.getOrder(request.orderId());
        Member member = memberService.getMember(order.getMemberId());

        long discountAmount = discountPolicyProvider.getPolicy(member.getGrade())
                .calculate(order.getOriginalPrice());
        long finalAmount = order.getOriginalPrice() - discountAmount;

        if (request.amount() != finalAmount) {
            throw new CustomException(new DomainError.PaymentAmountMismatch(finalAmount, request.amount()));
        }

        if (request.paymentMethod() == PaymentMethod.POINT) {
            memberService.deductPoint(member.getId(), finalAmount);
        }

        Payment payment = paymentRepository.save(
                Payment.create(order.getId(), finalAmount, request.paymentMethod()));
        order.markPaid();

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse read(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new CustomException(new DomainError.PaymentNotFound(paymentId)));
    }
}
