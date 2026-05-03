package com.cube.common.exception;

import org.springframework.http.HttpStatus;

public sealed interface DomainError {

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

    record DiscountPolicyNotFound(Long policyId) implements DomainError {
        public HttpStatus status() { return HttpStatus.NOT_FOUND; }
        public String code() { return "DISCOUNT_POLICY_NOT_FOUND"; }
        public String message() { return "할인 정책을 찾을 수 없습니다."; }
    }

    record PointNotEnough(Long memberId, long required, long available) implements DomainError {
        public HttpStatus status() { return HttpStatus.BAD_REQUEST; }
        public String code() { return "POINT_NOT_ENOUGH"; }
        public String message() { return "보유 포인트가 부족합니다."; }
    }

    record PaymentAmountMismatch(long expected, long actual) implements DomainError {
        public HttpStatus status() { return HttpStatus.BAD_REQUEST; }
        public String code() { return "PAYMENT_AMOUNT_MISMATCH"; }
        public String message() { return "결제 수단별 금액 합이 청구액과 다릅니다."; }
    }

    record PolicyVersionConflict(Long policyId, Long expectedVersion) implements DomainError {
        public HttpStatus status() { return HttpStatus.CONFLICT; }
        public String code() { return "POLICY_VERSION_CONFLICT"; }
        public String message() { return "다른 사용자가 정책을 수정했습니다. 다시 시도해 주세요."; }
    }

    record PointVersionConflict(Long memberId) implements DomainError {
        public HttpStatus status() { return HttpStatus.CONFLICT; }
        public String code() { return "POINT_VERSION_CONFLICT"; }
        public String message() { return "포인트 갱신 경합으로 결제에 실패했습니다. 다시 시도해 주세요."; }
    }

    record InvalidDiscountValue(Long policyId, String reason) implements DomainError {
        public HttpStatus status() { return HttpStatus.BAD_REQUEST; }
        public String code() { return "INVALID_DISCOUNT_VALUE"; }
        public String message() { return "할인 값이 정책 type 제약에 맞지 않습니다."; }
    }
}
