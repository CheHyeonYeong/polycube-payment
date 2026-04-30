package com.cube.payment.payment;

import com.cube.payment.common.response.ApiResponse;
import com.cube.payment.payment.request.PaymentCreateRequest;
import com.cube.payment.payment.response.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> pay(@RequestBody @Valid PaymentCreateRequest request) {
        return ApiResponse.ok(paymentService.pay(request));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> findById(@PathVariable Long paymentId) {
        return ApiResponse.ok(paymentService.findById(paymentId));
    }
}
