package com.apigateway.service;

public record RateLimitResult(
        boolean allowed,
        long remaining
) {
}