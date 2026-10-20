package com.apigateway.exception;

import com.apigateway.security.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleBackendUnavailable_returns502() {
        ResourceAccessException exception =
                new ResourceAccessException("Connection timed out");

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE))
                .thenReturn("test-correlation-id");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleBackendUnavailable(exception, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());

        assertEquals(
                "Backend service unavailable",
                response.getBody().get("message")
        );

        assertEquals(
                "test-correlation-id",
                response.getBody().get("correlationId")
        );

        assertEquals(
                "test-correlation-id",
                response.getHeaders()
                        .getFirst("X-Correlation-ID")
        );
    }

    @Test
    void handleRouteNotFound_returns404() {
        RouteNotFoundException exception =
                new RouteNotFoundException("Route not found");

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE))
                .thenReturn("test-correlation-id");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleRouteNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        assertEquals(
                "Route not found",
                response.getBody().get("message")
        );

        assertEquals(
                "test-correlation-id",
                response.getBody().get("correlationId")
        );

        assertEquals(
                "test-correlation-id",
                response.getHeaders()
                        .getFirst("X-Correlation-ID")
        );
    }
    @Test
    void handleMalformedJsonShouldReturnBadRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("correlationId")).thenReturn("test-correlation-id");

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Malformed JSON", (HttpInputMessage) null);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<Map<String, Object>> response =
                handler.handleMalformedJson(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed or invalid request body",
                response.getBody().get("message"));
        assertEquals("test-correlation-id",
                response.getBody().get("correlationId"));
        assertEquals("test-correlation-id",
                response.getHeaders().getFirst("X-Correlation-ID"));
    }
}