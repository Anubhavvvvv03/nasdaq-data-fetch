package com.hdfc.nasdaq_assignment.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 4xx - Business
    VALIDATION_FAILED("VALIDATION_FAILED", "Input validation failed", HttpStatus.BAD_REQUEST),
    STOCK_NOT_FOUND("STOCK_NOT_FOUND", "Stock not found", HttpStatus.NOT_FOUND),
    
    // 5xx - Technical
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus defaultStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus defaultStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.defaultStatus = defaultStatus;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
    public HttpStatus getDefaultStatus() { return defaultStatus; }
}
