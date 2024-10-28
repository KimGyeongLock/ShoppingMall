package com.trade_ham.common.exception;

import lombok.Getter;

@Getter
public class AccessDeniedException extends RuntimeException {

    private final ErrorCode errorCode;

    public AccessDeniedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}