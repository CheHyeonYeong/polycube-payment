package com.cube.common.exception;

import org.springframework.http.HttpStatus;

public sealed interface DomainError permits
        DomainError.MemberNotFound,
        DomainError.OrderNotFound,
        DomainError.PaymentNotFound,
        DomainError.InsufficientPoints,
        DomainError.PaymentAmountMismatch {

    HttpStatus status();
    String code();
    String message();

    record MemberNotFound(Long memberId) implements DomainError {
        public HttpStatus status() { return HttpStatus.NOT_FOUND; }
        public String code() { return "MEMBER_NOT_FOUND"; }
        public String message() { return "회원을 찾을 수 없습니다."; }
    }

    record OrderNotFound(Long orderId) implements DomainError {
        public HttpStatus status() { return HttpStatus.NOT_FOUND; }
        public String code() { return "ORDER_NOT_FOUND"; }
        public String message() { return "주문을 찾을 수 없습니다."; }
    }

    record PaymentNotFound(Long paymentId) implements DomainError {
        public HttpStatus status() { return HttpStatus.NOT_FOUND; }
        public String code() { return "PAYMENT_NOT_FOUND"; }
        public String message() { return "결제를 찾을 수 없습니다."; }
    }

    record InsufficientPoints(Long memberId, long required, long available) implements DomainError {
        public HttpStatus status() { return HttpStatus.BAD_REQUEST; }
        public String code() { return "INSUFFICIENT_POINTS"; }
        public String message() { return "보유 포인트가 부족합니다."; }
    }

    record PaymentAmountMismatch(long expected, long actual) implements DomainError {
        public HttpStatus status() { return HttpStatus.BAD_REQUEST; }
        public String code() { return "PAYMENT_AMOUNT_MISMATCH"; }
        public String message() { return "결제 금액이 할인 후 금액과 일치하지 않습니다."; }
    }
}
