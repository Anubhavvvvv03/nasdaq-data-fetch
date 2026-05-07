package com.hdfc.nasdaq_assignment.exception;

public abstract class InfrastructureException extends RuntimeException {
    private final String errorCode;

    protected InfrastructureException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    protected InfrastructureException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
