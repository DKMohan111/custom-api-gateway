package com.apigateway.controller;

import com.apigateway.security.CorrelationIdFilter;
import com.apigateway.service.GatewayService;
import com.apigateway.service.RateLimitResult;
import com.apigateway.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class GatewayControllerTest {

    private GatewayService gatewayService;
    private RateLimitService rateLimitService;
    private GatewayController gatewayController;

    @BeforeEach
    void setUp() {
        gatewayService = mock(GatewayService.class);
        rateLimitService = mock(RateLimitService.class);
        gatewayController = new GatewayController(
                gatewayService,
                rateLimitService
        );
    }

    @Test
    void getRequest_delegatesToGatewayService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");
        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);

        when(rateLimitResult.allowed()).thenReturn(true);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        when(gatewayService.forwardRequest(
                request.getRequestURI(),
                "test-correlation-id"))
                .thenReturn(
                        ResponseEntity.ok("{\"id\":42}")
                );

        ResponseEntity<String> response =
                gatewayController.forwardRequest(
                        request,
                        headers
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":42}", response.getBody());

        verify(gatewayService).forwardRequest(
                "/products/42",
                "test-correlation-id"
        );
    }
    @Test
    void getRequest_whenRateLimitExceeded_returns429() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(false);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        ResponseEntity<String> response =
                gatewayController.forwardRequest(request, new HttpHeaders());

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(
                "Too many requests. Please try again later.",
                response.getBody()
        );

        verify(gatewayService, never())
                .forwardRequest(anyString(), anyString());
    }
    @Test
    void getRequest_withQueryParams_forwardsFullPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        request.setQueryString("category=phone&page=1");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        when(gatewayService.forwardRequest(
                "/products?category=phone&page=1",
                "test-correlation-id"))
                .thenReturn(ResponseEntity.ok("products"));

        ResponseEntity<String> response =
                gatewayController.forwardRequest(
                        request,
                        new HttpHeaders()
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("products", response.getBody());

        verify(gatewayService).forwardRequest(
                "/products?category=phone&page=1",
                "test-correlation-id"
        );
    }
    @Test
    void postRequest_delegatesToGatewayService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"Phone\"}";

        when(gatewayService.forwardPostRequest(
                "/products",
                requestBody,
                headers))
                .thenReturn(ResponseEntity.ok("{\"id\":1}"));

        ResponseEntity<String> response =
                gatewayController.forwardPostRequest(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":1}", response.getBody());

        verify(gatewayService).forwardPostRequest(
                "/products",
                requestBody,
                headers
        );
    }
    @Test
    void postRequest_withQueryParams_forwardsFullPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        request.setQueryString("category=phone");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"Phone\"}";

        when(gatewayService.forwardPostRequest(
                "/products?category=phone",
                requestBody,
                headers))
                .thenReturn(ResponseEntity.ok("created"));

        ResponseEntity<String> response =
                gatewayController.forwardPostRequest(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("created", response.getBody());

        verify(gatewayService).forwardPostRequest(
                "/products?category=phone",
                requestBody,
                headers
        );
    }
    @Test
    void postRequest_whenRateLimitExceeded_returns429() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(false);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"name\":\"Phone\"}";

        ResponseEntity<String> response =
                gatewayController.forwardPostRequest(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(
                "Too many requests. Please try again later.",
                response.getBody()
        );

        verify(gatewayService, never())
                .forwardPostRequest(anyString(), anyString(), any(HttpHeaders.class));
    }
    @Test
    void putRequest_delegatesToGatewayService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"Updated Phone\"}";

        when(gatewayService.forwardPutRequest(
                "/products/42",
                requestBody,
                headers))
                .thenReturn(ResponseEntity.ok("{\"id\":42}"));

        ResponseEntity<String> response =
                gatewayController.put(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":42}", response.getBody());

        verify(gatewayService).forwardPutRequest(
                "/products/42",
                requestBody,
                headers
        );
    }
    @Test
    void putRequest_withQueryParams_forwardsFullPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");
        request.setQueryString("source=admin");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"Updated Phone\"}";

        when(gatewayService.forwardPutRequest(
                "/products/42?source=admin",
                requestBody,
                headers))
                .thenReturn(ResponseEntity.ok("updated"));

        ResponseEntity<String> response =
                gatewayController.put(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("updated", response.getBody());

        verify(gatewayService).forwardPutRequest(
                "/products/42?source=admin",
                requestBody,
                headers
        );
    }
    @Test
    void putRequest_whenRateLimitExceeded_returns429() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(false);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"name\":\"Updated Phone\"}";

        ResponseEntity<String> response =
                gatewayController.put(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNull(response.getBody());

        verify(gatewayService, never())
                .forwardPutRequest(
                        anyString(),
                        anyString(),
                        any(HttpHeaders.class)
                );
    }
    @Test
    void deleteRequest_delegatesToGatewayService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();

        when(gatewayService.forwardDeleteRequest(
                "/products/42",
                headers))
                .thenReturn(
                        ResponseEntity.ok()
                                .header("X-Backend", "backend-value")
                                .body("deleted")
                );

        ResponseEntity<String> response =
                gatewayController.delete(
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("deleted", response.getBody());

        assertEquals(
                "test-correlation-id",
                response.getHeaders().getFirst("X-Correlation-ID")
        );
        assertEquals(
                "5",
                response.getHeaders().getFirst("X-RateLimit-Limit")
        );
        assertEquals(
                "4",
                response.getHeaders().getFirst("X-RateLimit-Remaining")
        );
        assertEquals(
                "backend-value",
                response.getHeaders().getFirst("X-Backend")
        );

        verify(gatewayService).forwardDeleteRequest(
                "/products/42",
                headers
        );
    }
    @Test
    void deleteRequest_withQueryParams_forwardsFullPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");
        request.setQueryString("force=true");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();

        when(gatewayService.forwardDeleteRequest(
                "/products/42?force=true",
                headers))
                .thenReturn(ResponseEntity.ok("deleted"));

        ResponseEntity<String> response =
                gatewayController.delete(
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("deleted", response.getBody());

        verify(gatewayService).forwardDeleteRequest(
                "/products/42?force=true",
                headers
        );
    }
    @Test
    void deleteRequest_whenRateLimitExceeded_returns429() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(false);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();

        ResponseEntity<String> response =
                gatewayController.delete(
                        headers,
                        request
                );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNull(response.getBody());

        verify(gatewayService, never())
                .forwardDeleteRequest(
                        anyString(),
                        any(HttpHeaders.class)
                );
    }
    @Test
    void patchRequest_delegatesToGatewayService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"Patched Phone\"}";

        when(gatewayService.forwardPatchRequest(
                "/products/42",
                requestBody,
                headers))
                .thenReturn(ResponseEntity.ok("{\"id\":42}"));

        ResponseEntity<String> response =
                gatewayController.patch(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":42}", response.getBody());

        verify(gatewayService).forwardPatchRequest(
                "/products/42",
                requestBody,
                headers
        );
    }
    @Test
    void patchRequest_withQueryParams_forwardsFullPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");
        request.setQueryString("source=admin");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"Patched Phone\"}";

        when(gatewayService.forwardPatchRequest(
                "/products/42?source=admin",
                requestBody,
                headers))
                .thenReturn(ResponseEntity.ok("patched"));

        ResponseEntity<String> response =
                gatewayController.patch(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("patched", response.getBody());

        verify(gatewayService).forwardPatchRequest(
                "/products/42?source=admin",
                requestBody,
                headers
        );
    }
    @Test
    void patchRequest_whenRateLimitExceeded_returns429() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(false);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"name\":\"Patched Phone\"}";

        ResponseEntity<String> response =
                gatewayController.patch(
                        requestBody,
                        headers,
                        request
                );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(
                "Too many requests. Please try again later.",
                response.getBody()
        );

        verify(gatewayService, never())
                .forwardPatchRequest(
                        anyString(),
                        anyString(),
                        any(HttpHeaders.class)
                );
    }
    @Test
    void getRequest_forwardsResponseHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        ResponseEntity<String> serviceResponse =
                ResponseEntity.ok()
                        .header("X-Custom-Header", "test-value")
                        .body("products");

        when(gatewayService.forwardRequest(
                "/products/42",
                "test-correlation-id"))
                .thenReturn(serviceResponse);

        ResponseEntity<String> response =
                gatewayController.forwardRequest(
                        request,
                        new HttpHeaders()
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("products", response.getBody());
        assertEquals(
                "test-value",
                response.getHeaders().getFirst("X-Custom-Header")
        );
    }
    @Test
    void getRequest_addsCorrelationIdToResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(true);
        when(rateLimitResult.remaining()).thenReturn(4L);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        when(gatewayService.forwardRequest(
                "/products/42",
                "test-correlation-id"))
                .thenReturn(ResponseEntity.ok("products"));

        ResponseEntity<String> response =
                gatewayController.forwardRequest(
                        request,
                        new HttpHeaders()
                );

        assertEquals(
                "test-correlation-id",
                response.getHeaders().getFirst("X-Correlation-ID")
        );
    }
    @Test
    void getRequest_whenRateLimitExceeded_setsRateLimitHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");

        request.setAttribute(
                CorrelationIdFilter.CORRELATION_ID_ATTRIBUTE,
                "test-correlation-id"
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        RateLimitResult rateLimitResult = mock(RateLimitResult.class);
        when(rateLimitResult.allowed()).thenReturn(false);

        when(rateLimitService.checkLimit("test-user"))
                .thenReturn(rateLimitResult);

        ResponseEntity<String> response =
                gatewayController.forwardRequest(
                        request,
                        new HttpHeaders()
                );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(
                "test-correlation-id",
                response.getHeaders().getFirst("X-Correlation-ID")
        );
        assertEquals(
                "60",
                response.getHeaders().getFirst("Retry-After")
        );
        assertEquals(
                "5",
                response.getHeaders().getFirst("X-RateLimit-Limit")
        );
        assertEquals(
                "0",
                response.getHeaders().getFirst("X-RateLimit-Remaining")
        );
    }
}