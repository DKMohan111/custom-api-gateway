package com.apigateway.exception;

public class RateLimiterUnavailableException extends RuntimeException {

    public RateLimiterUnavailableException() {
        super("Rate limiter is unavailable");
    }
}