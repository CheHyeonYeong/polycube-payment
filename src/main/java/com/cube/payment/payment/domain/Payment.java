package com.cube.payment.payment.domain;

import com.cube.payment.order.domain.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 엔티티.
 *
 * 결제는 완료 이후 수정이 불가능해야 하므로 모든 필드를 updatable = false로 설정.
 * setter를 제공하지 않아 JPA 영속성 컨텍스트를 통한 의도치 않은 변경을 방지.
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
