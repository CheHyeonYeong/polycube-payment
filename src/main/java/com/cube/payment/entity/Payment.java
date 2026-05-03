package com.cube.payment.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.Hibernate;

import com.cube.common.PaymentMethod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "final_amount", nullable = false)
    private long finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    public static Payment create(Long orderId, long finalAmount, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.finalAmount = finalAmount;
        payment.paymentMethod = paymentMethod;
        payment.paidAt = Instant.now();
        return payment;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Payment other = (Payment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "Payment(id=" + id + ", method=" + paymentMethod + ", finalAmount=" + finalAmount + ")";
    }
}
