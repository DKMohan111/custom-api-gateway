
package com.apigateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {


    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_ATTRIBUTE =
            "correlationId";


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String requestCorrelationId = correlationId;

        request.setAttribute(
                CORRELATION_ID_ATTRIBUTE,
                requestCorrelationId
        );

        HttpServletRequest wrappedRequest =
                new HttpServletRequestWrapper(request) {

                    @Override
                    public String getHeader(String name) {
                        if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                            return requestCorrelationId;
                        }
                        return super.getHeader(name);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                            return Collections.enumeration(
                                    Collections.singletonList(requestCorrelationId));
                        }
                        return super.getHeaders(name);
                    }

                    @Override
                    public Enumeration<String> getHeaderNames() {
                        List<String> headerNames = new ArrayList<>();
                        Enumeration<String> existingNames = super.getHeaderNames();

                        while (existingNames != null && existingNames.hasMoreElements()) {
                            String name = existingNames.nextElement();

                            if (!CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                                headerNames.add(name);
                            }
                        }

                        headerNames.add(CORRELATION_ID_HEADER);

                        return Collections.enumeration(headerNames);
                    }
                };

        // Set it before processing so even error responses can include it.
        response.setHeader(CORRELATION_ID_HEADER, requestCorrelationId);

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            // Restore the same ID after downstream processing.
            response.setHeader(CORRELATION_ID_HEADER, requestCorrelationId);
        }
    }
}
