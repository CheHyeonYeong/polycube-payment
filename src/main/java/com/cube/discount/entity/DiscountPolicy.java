package com.cube.discount.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.Version;

import org.hibernate.Hibernate;

import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.discount.strategy.DiscountStrategies;
import com.cube.discount.strategy.DiscountStrategy;
import com.cube.member.entity.MemberGrade;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "discount_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscountPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_grade")
    private MemberGrade targetGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_payment_method")
    private PaymentMethod targetPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    private int priority;

    private boolean active;

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public static DiscountPolicy create(String name,
                                        MemberGrade targetGrade,
                                        PaymentMethod targetPaymentMethod,
                                        DiscountType discountType,
                                        BigDecimal discountValue,
                                        int priority) {
        DiscountPolicy policy = new DiscountPolicy();
        policy.name = name;
        policy.targetGrade = targetGrade;
        policy.targetPaymentMethod = targetPaymentMethod;
        policy.discountType = discountType;
        policy.discountValue = discountValue;
        policy.priority = priority;
        policy.active = true;
        Instant now = Instant.now();
        policy.createdAt = now;
        policy.updatedAt = now;
        return policy;
    }

    public DiscountStrategy toStrategy() {
        discountType.validate(discountValue)
                .ifPresent(reason -> {
                    throw new CustomException(new DomainError.InvalidDiscountValue(id, reason));
                });
        return switch (discountType) {
            case FIXED -> DiscountStrategies.fixed(this);
            case RATE -> DiscountStrategies.rate(this);
        };
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        DiscountPolicy other = (DiscountPolicy) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "DiscountPolicy(id=" + id + ", name=" + name + ", active=" + active + ")";
    }
}
