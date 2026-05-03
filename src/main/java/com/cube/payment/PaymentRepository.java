package com.cube.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cube.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
