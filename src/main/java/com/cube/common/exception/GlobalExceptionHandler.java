package com.cube.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustom(CustomException e) {
        DomainError error = e.getError();
        return ResponseEntity.status(error.status())
                .body(Map.of("code", error.code(), "message", error.message()));
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
                        "message", e.getMessage() == null ? "도메인 불변 조건을 위반했습니다." : e.getMessage()));
    }
}
