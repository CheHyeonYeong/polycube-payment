package com.cube.payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cube.payment.entity.PaymentDiscount;

public interface PaymentDiscountRepository extends JpaRepository<PaymentDiscount, Long> {
    List<PaymentDiscount> findByPaymentId(Long paymentId);
}
