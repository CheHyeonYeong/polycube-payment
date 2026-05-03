package com.cube.payment;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.payment.request.PaymentRequest;
import com.cube.payment.response.PaymentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.pay(request);
        URI location = URI.create("/api/v1/payments/" + response.paymentId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse read(@PathVariable Long paymentId) {
        return paymentService.read(paymentId);
    }
}
