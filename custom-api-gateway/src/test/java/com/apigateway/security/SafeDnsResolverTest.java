
package com.apigateway.security;

import org.apache.hc.client5.http.DnsResolver;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SafeDnsResolverTest {

    private SafeDnsResolver createResolver(String host, String ip,
                                           List<String> allowedTargets)
            throws Exception {
        DnsResolver delegate = mock(DnsResolver.class);

        when(delegate.resolve(host))
                .thenReturn(new InetAddress[]{
                        InetAddress.getByName(ip)
                });

        when(delegate.resolveCanonicalHostname(host))
                .thenReturn(host);

        return new SafeDnsResolver(delegate, allowedTargets);
    }

    @Test
    void rejectsLoopbackAddress() throws Exception {
        SafeDnsResolver resolver =
                createResolver("example.test", "127.0.0.1", List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void rejectsPrivateAddress() throws Exception {
        SafeDnsResolver resolver =
                createResolver("example.test", "192.168.1.10", List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void rejectsLinkLocalAddress() throws Exception {
        SafeDnsResolver resolver =
                createResolver("example.test", "169.254.169.254", List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void rejectsIpv6Loopback() throws Exception {
        SafeDnsResolver resolver =
                createResolver("example.test", "::1", List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void allowsPublicAddress() throws Exception {
        SafeDnsResolver resolver =
                createResolver("example.test", "8.8.8.8", List.of());

        InetAddress[] result = resolver.resolve("example.test");

        assertEquals(1, result.length);
        assertEquals("8.8.8.8", result[0].getHostAddress());
    }

    @Test
    void allowsConfiguredLocalBackend() throws Exception {
        SafeDnsResolver resolver =
                createResolver(
                        "localhost",
                        "127.0.0.1",
                        List.of("http://localhost:8081")
                );

        assertDoesNotThrow(() -> resolver.resolve("localhost"));
    }

    @Test
    void rejectsWhenAnyResolvedAddressIsPrivate() throws Exception {
        DnsResolver delegate = mock(DnsResolver.class);

        when(delegate.resolve("example.test"))
                .thenReturn(new InetAddress[]{
                        InetAddress.getByName("8.8.8.8"),
                        InetAddress.getByName("192.168.1.10")
                });

        SafeDnsResolver resolver =
                new SafeDnsResolver(delegate, List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void rejectsEmptyDnsResults() throws Exception {
        DnsResolver delegate = mock(DnsResolver.class);

        when(delegate.resolve("example.test"))
                .thenReturn(new InetAddress[0]);

        SafeDnsResolver resolver =
                new SafeDnsResolver(delegate, List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void propagatesUnknownHostException() throws Exception {
        DnsResolver delegate = mock(DnsResolver.class);

        when(delegate.resolve("missing.test"))
                .thenThrow(new UnknownHostException("Host not found"));

        SafeDnsResolver resolver =
                new SafeDnsResolver(delegate, List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("missing.test")
        );
    }

    @Test
    void rejectsIpv4MappedIpv6PrivateAddress() throws Exception {
        DnsResolver delegate = mock(DnsResolver.class);

        when(delegate.resolve("example.test"))
                .thenReturn(new InetAddress[]{
                        InetAddress.getByName("::ffff:192.168.1.10")
                });

        SafeDnsResolver resolver =
                new SafeDnsResolver(delegate, List.of());

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("example.test")
        );
    }

    @Test
    void allowsConfiguredDockerBackendPrivateIpv4() throws Exception {
        SafeDnsResolver resolver = createResolver(
                "backend-service",
                "172.20.0.5",
                List.of("http://backend-service:8081")
        );

        InetAddress[] result = resolver.resolve("backend-service");

        assertEquals(1, result.length);
        assertEquals("172.20.0.5", result[0].getHostAddress());
    }

    @Test
    void rejectsConfiguredDockerBackendPublicIpv4() throws Exception {
        SafeDnsResolver resolver = createResolver(
                "backend-service",
                "8.8.8.8",
                List.of("http://backend-service:8081")
        );

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("backend-service")
        );
    }

    @Test
    void rejectsDockerBackendIpv6Address() throws Exception {
        SafeDnsResolver resolver = createResolver(
                "backend-service",
                "2001:4860:4860::8888",
                List.of("http://backend-service:8081")
        );

        assertThrows(
                UnknownHostException.class,
                () -> resolver.resolve("backend-service")
        );
    }

}
