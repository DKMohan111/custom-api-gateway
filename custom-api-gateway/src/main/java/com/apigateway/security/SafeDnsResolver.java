
package com.apigateway.security;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class SafeDnsResolver implements DnsResolver {

    private final DnsResolver delegate;
    private final Set<String> trustedLocalHosts;
    private final Set<String> trustedDockerHosts;

    @Autowired
    public SafeDnsResolver(
            @Value("${gateway.security.allowed-targets:}")
            List<String> allowedTargets) {
        this(new SystemDefaultDnsResolver(), allowedTargets);
    }

    // Constructor used by unit tests.
    SafeDnsResolver(
            DnsResolver delegate,
            List<String> allowedTargets) {

        this.delegate = delegate;

        Set<String> localHosts = new HashSet<>();
        Set<String> dockerHosts = new HashSet<>();

        for (String target : allowedTargets) {
            if (target == null || target.isBlank()) {
                continue;
            }

            URI uri;
            try {
                uri = URI.create(target.trim());
            } catch (IllegalArgumentException exception) {
                continue;
            }

            if (!"http".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null
                    || uri.getPort() != 8081) {
                continue;
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);

            if ("localhost".equals(host)) {
                localHosts.add(host);
            } else if ("backend-service".equals(host)) {
                dockerHosts.add(host);
            }
        }

        this.trustedLocalHosts = Set.copyOf(localHosts);
        this.trustedDockerHosts = Set.copyOf(dockerHosts);
    }

    @Override
    public InetAddress[] resolve(String host)
            throws UnknownHostException {

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        InetAddress[] addresses = delegate.resolve(host);

        if (addresses == null || addresses.length == 0) {
            throw new UnknownHostException(
                    "No addresses found for host: " + host
            );
        }

        if (trustedLocalHosts.contains(normalizedHost)) {
            for (InetAddress address : addresses) {
                if (!address.isLoopbackAddress()) {
                    throw new UnknownHostException(
                            "Trusted localhost resolved to a non-loopback address"
                    );
                }
            }
            return addresses;
        }

        if (trustedDockerHosts.contains(normalizedHost)) {
            for (InetAddress address : addresses) {
                if (!isDockerPrivateAddress(address)) {
                    throw new UnknownHostException(
                            "Trusted Docker backend resolved to an unexpected address"
                    );
                }
            }
            return addresses;
        }

        for (InetAddress address : addresses) {
            if (isBlockedAddress(address)) {
                throw new UnknownHostException(
                        "Blocked local or private destination address: "
                                + address.getHostAddress()
                );
            }
        }

        return addresses;
    }

    @Override
    public String resolveCanonicalHostname(String host)
            throws UnknownHostException {
        return delegate.resolveCanonicalHostname(host);
    }

    private boolean isDockerPrivateAddress(InetAddress address) {
        return address instanceof Inet4Address
                && address.isSiteLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isAnyLocalAddress()
                && !address.isLinkLocalAddress()
                && !address.isMulticastAddress();
    }

    private boolean isBlockedAddress(InetAddress address) {
        byte[] bytes = address.getAddress();

        boolean uniqueLocalIpv6 =
                bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC;

        return address.isLoopbackAddress()
                || address.isAnyLocalAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || uniqueLocalIpv6;
    }
}
