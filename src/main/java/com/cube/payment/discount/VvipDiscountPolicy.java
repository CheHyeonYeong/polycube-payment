package com.cube.payment.discount;

import com.cube.payment.order.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class VvipDiscountPolicy implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.10;

    @Override
    public long calculateDiscountAmount(Order order) {
        return (long) (order.getOriginalPrice() * DISCOUNT_RATE);
    }

    @Override
    public String getPolicyName() {
        return "VVIP_RATE_10_PERCENT";
    }
}
