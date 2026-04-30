package com.cube.payment.payment.entity;

import com.cube.payment.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 엔티티.
 *
 * 결제 완료 후 금액 관련 필드는 변경 불가 (updatable = false).
 * setter를 제공하지 않아 의도치 않은 데이터 변경을 방지한다.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Column(nullable = false, updatable = false)
    private long discountAmount;

    @Column(nullable = false, updatable = false)
    private long finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paidAt;

    public Payment(Order order, long discountAmount, long finalAmount,
                   PaymentMethod paymentMethod, LocalDateTime paidAt) {
        this.order = order;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
    }
}
