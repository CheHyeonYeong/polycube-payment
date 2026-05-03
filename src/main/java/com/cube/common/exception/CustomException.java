package com.cube.common.exception;

import lombok.Getter;

/** sealed DomainError ADT 를 캡슐화 — handler 가 status/code/message/details 를 직렬화. */
@Getter
public final class CustomException extends RuntimeException {

    private final DomainError error;

    public CustomException(DomainError error) {
        super(error.message());
        this.error = error;
    }
}
