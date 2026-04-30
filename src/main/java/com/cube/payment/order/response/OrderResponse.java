package com.cube.payment.order.response;

import com.cube.payment.order.entity.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderResponse {

    private final Long id;
    private final Long memberId;
    private final String productName;
    private final long originalPrice;

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getMember().getId(),
                order.getProductName(),
                order.getOriginalPrice()
        );
    }
}
