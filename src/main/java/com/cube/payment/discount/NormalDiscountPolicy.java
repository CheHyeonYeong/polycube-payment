package com.cube.payment.discount;

import com.cube.payment.order.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class NormalDiscountPolicy implements DiscountPolicy {

    @Override
    public long calculateDiscountAmount(Order order) {
        return 0L;
    }

    @Override
    public String getPolicyName() {
        return "NORMAL_NO_DISCOUNT";
    }
}
