package com.apigateway.controller;

import com.apigateway.security.CorrelationIdFilter;
import com.apigateway.service.GatewayService;
import com.apigateway.service.RateLimitResult;
import com.apigateway.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
public class GatewayController {

    private static final Logger logger =
            LoggerFactory.getLogger(GatewayController.class);

    private final GatewayService gatewayService;
    private final RateLimitService rateLimitService;

    public GatewayController(
            GatewayService gatewayService,
            RateLimitService rateLimitService) {

        this.gatewayService = gatewayService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/**")
    public ResponseEntity<String> forwardRequest(
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers) {

        String correlationId = (String) request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE
        );

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        RateLimitResult result = rateLimitService.checkLimit(username);

        String path = request.getRequestURI();

        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }

        if (!result.allowed()) {
            logger.warn(
                    "Rate limit exceeded | correlationId={} | username={} | method=GET | path={}",
                    correlationId, username, path
            );

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Correlation-ID", correlationId)
                    .header("Retry-After", "60")
                    .header("X-RateLimit-Limit", "5")
                    .header("X-RateLimit-Remaining", "0")
                    .body("Too many requests. Please try again later.");
        }

        logger.info(
                "Incoming request | correlationId={} | method=GET | path={}",
                correlationId, path
        );

        ResponseEntity<String> response =
                gatewayService.forwardRequest(path, correlationId);

        logger.info(
                "Response received | correlationId={} | status={}",
                correlationId, response.getStatusCode().value()
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .header("X-Correlation-ID", correlationId)
                .header("X-RateLimit-Limit", "5")
                .header(
                        "X-RateLimit-Remaining",
                        String.valueOf(result.remaining())
                )
                .body(response.getBody());
    }

    @PostMapping("/**")
    public ResponseEntity<String> forwardPostRequest(
            @RequestBody String requestBody,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {

        String correlationId = (String) request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        RateLimitResult result = rateLimitService.checkLimit(username);

        String path = request.getRequestURI();

        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }

        if (!result.allowed()) {
            logger.warn(
                    "Rate limit exceeded | correlationId={} | username={} | method=POST | path={}",
                    correlationId, username, path);

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Correlation-ID", correlationId)
                    .header("Retry-After", "60")
                    .header("X-RateLimit-Limit", "5")
                    .header("X-RateLimit-Remaining", "0")
                    .body("Too many requests. Please try again later.");
        }

        logger.info(
                "Incoming request | correlationId={} | method=POST | path={}",
                correlationId, path);

        ResponseEntity<String> response =
                gatewayService.forwardPostRequest(path, requestBody, headers);

        logger.info(
                "Response received | correlationId={} | status={}",
                correlationId, response.getStatusCode().value());

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .header("X-Correlation-ID", correlationId)
                .header("X-RateLimit-Limit", "5")
                .header("X-RateLimit-Remaining",
                        String.valueOf(result.remaining()))
                .body(response.getBody());
    }

    @PutMapping("/**")
    public ResponseEntity<String> put(
            @RequestBody String requestBody,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {

        String path = request.getRequestURI();

        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }

        String correlationId = (String) request.getAttribute(
                    CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE
        );

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        RateLimitResult result = rateLimitService.checkLimit(username);

        if (!result.allowed()) {
            logger.warn(
                    "Rate limit exceeded | correlationId={} | username={} | method=PUT | path={}",
                    correlationId, username, path
            );

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Correlation-ID", correlationId)
                    .header("Retry-After", "60")
                    .header("X-RateLimit-Limit", "5")
                    .header("X-RateLimit-Remaining", "0")
                    .build();
        }

        logger.info(
                "Incoming request | correlationId={} | method=PUT | path={}",
                correlationId, path
        );

        ResponseEntity<String> response =
                gatewayService.forwardPutRequest(path, requestBody, headers);

        logger.info(
                "Response received | correlationId={} | status={}",
                correlationId, response.getStatusCode().value()
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .header("X-Correlation-ID", correlationId)
                .header("X-RateLimit-Limit", "5")
                .header("X-RateLimit-Remaining",
                        String.valueOf(result.remaining()))
                .body(response.getBody());
    }

    @DeleteMapping("/**")
    public ResponseEntity<String> delete(
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {

        String path = request.getRequestURI();

        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }

            String correlationId = (String) request.getAttribute(
                    CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE
            );

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        RateLimitResult result = rateLimitService.checkLimit(username);

        if (!result.allowed()) {
            logger.warn(
                    "Rate limit exceeded | correlationId={} | username={} | method=DELETE | path={}",
                    correlationId, username, path
            );

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Correlation-ID", correlationId)
                    .header("Retry-After", "60")
                    .header("X-RateLimit-Limit", "5")
                    .header("X-RateLimit-Remaining", "0")
                    .build();
        }

        logger.info(
                "Incoming request | correlationId={} | method=DELETE | path={}",
                correlationId, path
        );

        ResponseEntity<String> response =
                gatewayService.forwardDeleteRequest(path, headers);

        logger.info(
                "Response received | correlationId={} | status={}",
                correlationId, response.getStatusCode().value()
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .header("X-Correlation-ID", correlationId)
                .header("X-RateLimit-Limit", "5")
                .header("X-RateLimit-Remaining",
                        String.valueOf(result.remaining()))
                .body(response.getBody());
    }

    @PatchMapping("/**")
    public ResponseEntity<String> patch(
            @RequestBody String requestBody,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {

        String path = request.getRequestURI();

        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }

        String correlationId = (String) request.getAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE);

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        RateLimitResult result = rateLimitService.checkLimit(username);

        if (!result.allowed()) {
            logger.warn(
                    "Rate limit exceeded | correlationId={} | username={} | method=PATCH | path={}",
                    correlationId, username, path);

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Correlation-ID", correlationId)
                    .header("Retry-After", "60")
                    .header("X-RateLimit-Limit", "5")
                    .header("X-RateLimit-Remaining", "0")
                    .body("Too many requests. Please try again later.");
        }

        logger.info(
                "Incoming request | correlationId={} | method=PATCH | path={}",
                correlationId, path);

        ResponseEntity<String> response =
                gatewayService.forwardPatchRequest(path, requestBody, headers);

        logger.info(
                "Response received | correlationId={} | status={}",
                correlationId, response.getStatusCode().value());

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .header("X-Correlation-ID", correlationId)
                .header("X-RateLimit-Limit", "5")
                .header("X-RateLimit-Remaining",
                        String.valueOf(result.remaining()))
                .body(response.getBody());
    }

}