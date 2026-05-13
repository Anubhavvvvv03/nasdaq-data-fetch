package com.hdfc.nasdaq_assignment.exception;

public class AuthException extends BusinessException {
    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
