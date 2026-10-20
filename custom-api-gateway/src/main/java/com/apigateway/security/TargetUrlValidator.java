
        package com.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class TargetUrlValidator {

    private final List<URI> allowedTargets;

    @Autowired
    public TargetUrlValidator(
            @Value("${gateway.security.allowed-targets:}")
            List<String> allowedTargets) {

        this.allowedTargets = allowedTargets.stream()
                .map(String::trim)
                .filter(target -> !target.isEmpty())
                .map(URI::create)
                .peek(this::validateConfiguredTarget)
                .toList();

        if (this.allowedTargets.isEmpty()) {
            throw new IllegalStateException(
                    "At least one gateway target must be configured"
            );
        }
    }

    // Retained for unit tests that exercise an empty allowlist.
    public TargetUrlValidator() {
        this.allowedTargets = List.of();
    }

    private void validateConfiguredTarget(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (scheme == null
                || (!scheme.equalsIgnoreCase("http")
                && !scheme.equalsIgnoreCase("https"))
                || host == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null
                || uri.getQuery() != null
                || (uri.getPort() != -1
                && (uri.getPort() < 1 || uri.getPort() > 65535))) {
            throw new IllegalStateException(
                    "Invalid configured gateway target"
            );
        }
    }

    public void validate(String targetUrl) {
        try {
            URI target = URI.create(targetUrl);
            validateTargetSyntax(target);

            boolean allowed = allowedTargets.stream()
                    .anyMatch(configured -> sameOrigin(configured, target));

            if (!allowed) {
                throw new IllegalArgumentException(
                        "Target origin is not allowlisted"
                );
            }

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid or non-allowlisted target URL",
                    exception
            );
        }
    }

    private void validateTargetSyntax(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (scheme == null
                || (!scheme.equalsIgnoreCase("http")
                && !scheme.equalsIgnoreCase("https"))
                || host == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null
                || (uri.getPort() != -1
                && (uri.getPort() < 1 || uri.getPort() > 65535))) {
            throw new IllegalArgumentException(
                    "Target URL must use HTTP/HTTPS and contain a valid host"
            );
        }
    }

    private boolean sameOrigin(URI configured, URI target) {
        return configured.getScheme().equalsIgnoreCase(target.getScheme())
                && configured.getHost().equalsIgnoreCase(target.getHost())
                && effectivePort(configured) == effectivePort(target);
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }

        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}