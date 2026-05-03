package com.cube.order.response;

import java.time.Instant;

import com.cube.order.entity.Order;
import com.cube.order.entity.OrderStatus;

public record OrderResponse(
        Long id,
        String productName,
        long originalPrice,
        Long memberId,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProductName(),
                order.getOriginalPrice(),
                order.getMemberId(),
                order.getStatus(),
                order.getCreatedAt());
    }
}
