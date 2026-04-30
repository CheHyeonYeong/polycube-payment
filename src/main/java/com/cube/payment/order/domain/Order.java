package com.cube.payment.order.domain;

import com.cube.payment.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private long originalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public Order(String productName, long originalPrice, Member member) {
        if (originalPrice < 0) {
            throw new IllegalArgumentException("주문 원가는 0 이상이어야 합니다.");
        }
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.member = member;
    }
}
