package com.hdfc.nasdaq_assignment.exception;

import com.hdfc.nasdaq_assignment.dto.ValidationError;
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
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setProperty("errorCode", ex.getErrorCode());
        pd.setProperty("traceId", getTraceId());
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        
        // Include timeTaken if available
        Long startTime = (Long) request.getAttribute("startTime", WebRequest.SCOPE_REQUEST);
        if (startTime != null) {
            pd.setProperty("timeTaken", (System.currentTimeMillis() - startTime) + " ms");
        }

        log.warn("Business error: {} - {}", ex.getErrorCode(), ex.getMessage());
        return pd;
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
        
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail(isProduction() ? "An unexpected error occurred" : ex.getMessage());
        pd.setProperty("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        pd.setProperty("traceId", getTraceId());
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return pd;
    }

    private String getTraceId() {
        return MDC.get("traceId") != null ? MDC.get("traceId") : "N/A";
    }

    private boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
