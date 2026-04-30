package com.cube.payment.order;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.member.MemberRepository;
import com.cube.payment.member.entity.Member;
import com.cube.payment.order.entity.Order;
import com.cube.payment.order.request.OrderCreateRequest;
import com.cube.payment.order.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Order order = new Order(request.getProductName(), request.getOriginalPrice(), member);
        return OrderResponse.from(orderRepository.save(order));
    }

    public OrderResponse findById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }
}
