package com.cube.payment;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.payment.PaymentService.PayResult;
import com.cube.payment.request.PaymentRequest;
import com.cube.payment.response.PaymentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        PayResult result = paymentService.pay(idempotencyKey, request);
        URI location = URI.create("/api/v1/payments/" + result.response().paymentId());
        return result.newlyCreated()
                ? ResponseEntity.created(location).body(result.response())
                : ResponseEntity.ok().location(location).body(result.response());
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse read(@PathVariable Long paymentId) {
        return paymentService.read(paymentId);
    }
}
