package com.cube.payment.entity;

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

import org.hibernate.Hibernate;

import com.cube.common.PaymentMethod;
import com.cube.discount.entity.DiscountType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment_discount")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_name")
    private String policyName;

    @Column(name = "policy_version")
    private Long policyVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "discount_amount")
    private long discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "applied_scope")
    private AppliedScope appliedScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "applied_method")
    private PaymentMethod appliedMethod;

    @Column(name = "applied_at")
    private Instant appliedAt;

    public static PaymentDiscount snapshot(Long paymentId,
                                           DiscountSnapshotData data,
                                           AppliedScope scope,
                                           PaymentMethod method,
                                           Instant appliedAt) {
        PaymentDiscount snapshot = new PaymentDiscount();
        snapshot.paymentId = paymentId;
        snapshot.policyId = data.policyId();
        snapshot.policyName = data.policyName();
        snapshot.policyVersion = data.policyVersion();
        snapshot.discountType = data.discountType();
        snapshot.discountValue = data.discountValue();
        snapshot.discountAmount = data.discountAmount();
        snapshot.appliedScope = scope;
        snapshot.appliedMethod = method;
        snapshot.appliedAt = appliedAt;
        return snapshot;
    }

    public record DiscountSnapshotData(
            Long policyId,
            String policyName,
            Long policyVersion,
            DiscountType discountType,
            BigDecimal discountValue,
            long discountAmount
    ) {
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PaymentDiscount other = (PaymentDiscount) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "PaymentDiscount(id=" + id + ", scope=" + appliedScope
                + ", policyName=" + policyName + ", amount=" + discountAmount + ")";
    }
}
