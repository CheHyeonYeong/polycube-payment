package com.cube.payment.order;

import com.cube.payment.common.response.ApiResponse;
import com.cube.payment.order.request.OrderCreateRequest;
import com.cube.payment.order.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(@RequestBody @Valid OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> findById(@PathVariable Long orderId) {
        return ApiResponse.ok(orderService.findById(orderId));
    }
}
