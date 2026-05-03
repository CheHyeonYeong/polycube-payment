package com.cube.discount.strategy;

import com.cube.common.Money;
import com.cube.common.PaymentMethod;
import com.cube.member.entity.MemberGrade;

public record DiscountContext(
        Money currentPrice,
        MemberGrade memberGrade,
        PaymentMethod paymentMethod
) {
    public static DiscountContext of(Money price, MemberGrade grade) {
        return new DiscountContext(price, grade, null);
    }

    public DiscountContext withCurrentPrice(Money newPrice) {
        return new DiscountContext(newPrice, memberGrade, paymentMethod);
    }

    public DiscountContext withPaymentMethod(PaymentMethod method) {
        return new DiscountContext(currentPrice, memberGrade, method);
    }
}
