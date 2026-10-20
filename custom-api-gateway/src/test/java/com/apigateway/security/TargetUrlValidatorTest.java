
package com.apigateway.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TargetUrlValidatorTest {

    private final TargetUrlValidator validator = new TargetUrlValidator();


    @Test
    void validate_withHttpUrl_doesNotThrow() {
        TargetUrlValidator configuredValidator =
                new TargetUrlValidator(
                        List.of("http://8.8.8.8")
                );

        assertDoesNotThrow(() ->
                configuredValidator.validate("http://8.8.8.8/products")
        );
    }

    @Test
    void validate_withHttpsUrl_doesNotThrow() {
        TargetUrlValidator configuredValidator =
                new TargetUrlValidator(
                        List.of("https://8.8.8.8")
                );

        assertDoesNotThrow(() ->
                configuredValidator.validate(
                        "https://8.8.8.8/products"
                )
        );
    }

    @Test
    void validate_withUnsupportedScheme_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("ftp://example.com/products")
        );
    }

    @Test
    void validate_withoutHost_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http:///products")
        );
    }

    @Test
    void rejectsLocalhost() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://localhost:8081/products")
        );
    }

    @Test
    void rejectsPrivateIpAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://192.168.1.10/products")
        );
    }

    @Test
    void rejectsMetadataServiceIp() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "http://169.254.169.254/latest/meta-data"
                )
        );
    }

    @Test
    void rejectsLoopbackIpAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://127.0.0.1:8081/products")
        );
    }

    @Test
    void rejectsLinkLocalIpAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://169.254.1.10/products")
        );
    }

    @Test
    void rejectsIpv6Loopback() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://[::1]:8081/products")
        );
    }

    @Test
    void rejectsIpv6PrivateAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://[fc00::1]:8081/products")
        );
    }

    @Test
    void rejectsUnspecifiedIpAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://0.0.0.0:8081/products")
        );
    }

    @Test
    void rejectsIpv6UnspecifiedAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("http://[::]:8081/products")
        );
    }

    @Test
    void allowsConfiguredLocalBackend() {
        TargetUrlValidator configuredValidator =
                new TargetUrlValidator(
                        List.of("http://localhost:8081")
                );

        assertDoesNotThrow(
                () -> configuredValidator.validate(
                        "http://localhost:8081/products"
                )
        );
    }

    @Test
    void rejectsLocalhostOnUnconfiguredPort() {
        TargetUrlValidator configuredValidator =
                new TargetUrlValidator(
                        List.of("http://localhost:8081")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> configuredValidator.validate(
                        "http://localhost:8082/products"
                )
        );
    }

    @Test
    void rejectsPrivateIpWhenNotAllowlisted() {
        TargetUrlValidator configuredValidator =
                new TargetUrlValidator(
                        List.of("http://localhost:8081")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> configuredValidator.validate(
                        "http://192.168.1.10/products"
                )
        );
    }

    @Test
    void rejectsUrlWithUserInfo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "http://user:password@8.8.8.8/products"
                )
        );
    }

    @Test
    void rejectsUrlWithFragment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(
                        "http://8.8.8.8/products#internal"
                )
        );
    }

}
