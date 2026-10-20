package com.apigateway.exception;

import com.apigateway.security.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        String correlationId = (String) request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("correlationId", correlationId);

        return ResponseEntity.status(status)
                .header("X-Correlation-ID", correlationId)
                .body(response);
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRouteNotFound(
            RouteNotFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateRouteException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateRoute(
            DuplicateRouteException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.put("message", "Validation failed");

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                )
        );

        response.put("details", errors);

        String correlationId = (String) request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE
        );
        response.put("correlationId", correlationId);

        return ResponseEntity.badRequest()
                .header("X-Correlation-ID", correlationId)
                .body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleBackendUnavailable(
            ResourceAccessException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "Backend service unavailable",
                request
        );
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                request
        );
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed or invalid request body",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }
}
