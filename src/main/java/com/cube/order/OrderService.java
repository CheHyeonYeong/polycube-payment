package com.cube.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.member.MemberService;
import com.cube.order.entity.Order;
import com.cube.order.request.OrderCreateRequest;
import com.cube.order.response.OrderResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        memberService.getMember(request.memberId());
        Order order = orderRepository.save(
                Order.create(request.productName(), request.originalPrice(), request.memberId()));
        return OrderResponse.from(order);
    }

    public OrderResponse read(Long orderId) {
        return OrderResponse.from(getOrder(orderId));
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(new DomainError.OrderNotFound(orderId)));
    }
}
