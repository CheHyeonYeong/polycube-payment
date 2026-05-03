package com.cube.payment.entity;

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
@Table(name = "payment_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "gross_amount")
    private long grossAmount;

    @Column(name = "charged_amount")
    private long chargedAmount;

    public static PaymentDetail create(Long paymentId, PaymentMethod method, long grossAmount, long chargedAmount) {
        PaymentDetail detail = new PaymentDetail();
        detail.paymentId = paymentId;
        detail.paymentMethod = method;
        detail.grossAmount = grossAmount;
        detail.chargedAmount = chargedAmount;
        return detail;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PaymentDetail other = (PaymentDetail) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "PaymentDetail(id=" + id + ", method=" + paymentMethod + ", charged=" + chargedAmount + ")";
    }
}
