package com.hdfc.nasdaq_assignment.config;

import com.hdfc.nasdaq_assignment.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, authException.getMessage());
        pd.setTitle("Unauthorized");
        pd.setProperty("errorCode", ErrorCode.UNAUTHORIZED.getCode());
        pd.setProperty("traceId", MDC.get("traceId") != null ? MDC.get("traceId") : "N/A");
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setInstance(URI.create(request.getRequestURI()));

        objectMapper.writeValue(response.getOutputStream(), pd);
    }
}
