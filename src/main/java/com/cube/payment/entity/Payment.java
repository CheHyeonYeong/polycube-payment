package com.cube.payment.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.Hibernate;

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

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "original_amount")
    private long originalAmount;

    @Column(name = "discount_amount")
    private long discountAmount;

    @Column(name = "final_amount")
    private long finalAmount;

    @Column(name = "paid_at")
    private Instant paidAt;

    public static Payment create(String idempotencyKey,
                                 Long orderId,
                                 long originalAmount,
                                 long discountAmount,
                                 long finalAmount,
                                 Instant paidAt) {
        Payment payment = new Payment();
        payment.idempotencyKey = idempotencyKey;
        payment.orderId = orderId;
        payment.originalAmount = originalAmount;
        payment.discountAmount = discountAmount;
        payment.finalAmount = finalAmount;
        payment.paidAt = paidAt;
        return payment;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Payment other = (Payment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "Payment(id=" + id + ", idempotencyKey=" + idempotencyKey + ")";
    }
}
