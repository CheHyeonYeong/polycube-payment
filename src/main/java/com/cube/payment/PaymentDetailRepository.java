package com.cube.payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cube.payment.entity.PaymentDetail;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
    List<PaymentDetail> findByPaymentId(Long paymentId);
}
