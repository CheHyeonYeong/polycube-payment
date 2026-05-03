package com.cube.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustom(CustomException e) {
        DomainError error = e.getError();
        return ResponseEntity.status(error.status()).body(toBody(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "INVALID_REQUEST");
        body.put("message", "요청 값 검증에 실패했습니다.");
        body.put("fields", e.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "reason", String.valueOf(fe.getDefaultMessage())))
                .toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "code", "MISSING_HEADER",
                        "message", "필수 헤더가 누락되었습니다.",
                        "header", e.getHeaderName()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("code", "INVALID_REQUEST", "message", e.getMessage()));
    }

    @ExceptionHandler(InvariantViolation.class)
    public ResponseEntity<Map<String, Object>> handleInvariantViolation(InvariantViolation e) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "code", "DOMAIN_INVARIANT_VIOLATED",
                        "message", e.getMessage() == null ? "도메인 invariant 를 위반했습니다." : e.getMessage()));
    }

    private Map<String, Object> toBody(DomainError error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", error.code());
        body.put("message", error.message());
        Map<String, Object> details = detailsOf(error);
        body.putAll(details);
        return body;
    }

    private Map<String, Object> detailsOf(DomainError error) {
        return switch (error) {
            case DomainError.MemberNotFound m -> Map.of("memberId", m.memberId());
            case DomainError.OrderNotFound o -> Map.of("orderId", o.orderId());
            case DomainError.PaymentNotFound p -> Map.of("paymentId", p.paymentId());
            case DomainError.DiscountPolicyNotFound d -> Map.of("policyId", d.policyId());
            case DomainError.PointNotEnough p -> Map.of(
                    "memberId", p.memberId(),
                    "required", p.required(),
                    "available", p.available());
            case DomainError.PaymentAmountMismatch m -> Map.of(
                    "expected", m.expected(),
                    "actual", m.actual());
            case DomainError.PolicyVersionConflict v -> Map.of(
                    "policyId", v.policyId(),
                    "expectedVersion", v.expectedVersion());
            case DomainError.PointVersionConflict v -> Map.of("memberId", v.memberId());
            case DomainError.InvalidDiscountValue v -> Map.of(
                    "policyId", v.policyId(),
                    "reason", v.reason());
        };
    }
}
