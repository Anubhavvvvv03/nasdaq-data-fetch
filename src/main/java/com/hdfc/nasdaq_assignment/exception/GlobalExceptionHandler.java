package com.hdfc.nasdaq_assignment.exception;

import com.hdfc.nasdaq_assignment.dto.ValidationError;
import com.hdfc.nasdaq_assignment.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Environment environment;

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex, WebRequest request) {
        log.warn("Business error: {} - {}", ex.getErrorCode(), ex.getMessage());
        return createProblemDetail(ex.getStatus(), ex.getClass().getSimpleName(), ex.getMessage(), ex.getErrorCode(), request);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(org.springframework.security.core.AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Authentication Failed", ex.getMessage(), ErrorCode.UNAUTHORIZED.getCode(), request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage(), ErrorCode.ACCESS_DENIED.getCode(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle("Validation Failed");
        pd.setDetail("Input validation failed");
        pd.setProperty("errorCode", ErrorCode.VALIDATION_FAILED.getCode());
        pd.setProperty("traceId", getTraceId());
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                .toList();
        pd.setProperty("errors", errors);

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        String detail = isProduction() ? "An unexpected error occurred" : ex.getMessage();
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", detail, ErrorCode.INTERNAL_SERVER_ERROR.getCode(), request);
    }

    private ProblemDetail createProblemDetail(HttpStatusCode status, String title, String detail, String errorCode, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("errorCode", errorCode);
        pd.setProperty("traceId", getTraceId());
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        Long startTime = (Long) request.getAttribute("startTime", WebRequest.SCOPE_REQUEST);
        if (startTime != null) {
            pd.setProperty("timeTaken", (System.currentTimeMillis() - startTime) + " ms");
        }
        return pd;
    }

    private String getTraceId() {
        return MDC.get("traceId") != null ? MDC.get("traceId") : "N/A";
    }

    private boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
