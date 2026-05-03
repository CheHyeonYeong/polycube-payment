package com.cube.order.entity;

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

import com.cube.common.exception.InvariantViolation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "original_price")
    private long originalPrice;

    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static Order create(String productName, long originalPrice, Long memberId) {
        if (productName == null || productName.isBlank()) {
            throw new InvariantViolation("상품명은 비어 있을 수 없습니다.");
        }
        if (originalPrice < 0) {
            throw new InvariantViolation("주문 원가는 0 이상이어야 합니다: " + originalPrice);
        }
        if (memberId == null) {
            throw new InvariantViolation("회원 식별자는 필수입니다.");
        }
        Order order = new Order();
        order.productName = productName;
        order.originalPrice = originalPrice;
        order.memberId = memberId;
        order.status = OrderStatus.CREATED;
        order.createdAt = Instant.now();
        return order;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Order other = (Order) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "Order(id=" + id + ", productName=" + productName + ", status=" + status + ")";
    }
}
