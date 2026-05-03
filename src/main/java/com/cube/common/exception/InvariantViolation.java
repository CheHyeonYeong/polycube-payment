package com.cube.common.exception;

/** 도메인 invariant 위반. 라이브러리 IAE 와 분리되어 GlobalExceptionHandler 가 400 으로 매핑. */
public final class InvariantViolation extends RuntimeException {

    public InvariantViolation(String message) {
        super(message);
    }

    public InvariantViolation(String message, Throwable cause) {
        super(message, cause);
    }
}
