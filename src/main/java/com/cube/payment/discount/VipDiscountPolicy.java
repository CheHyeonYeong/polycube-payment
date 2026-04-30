package com.cube.payment.discount;

import com.cube.payment.order.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class VipDiscountPolicy implements DiscountPolicy {

    private static final long DISCOUNT_AMOUNT = 1_000L;

    @Override
    public long calculateDiscountAmount(Order order) {
        return Math.min(DISCOUNT_AMOUNT, order.getOriginalPrice());
    }

    @Override
    public String getPolicyName() {
        return "VIP_FIXED_1000";
    }
}
