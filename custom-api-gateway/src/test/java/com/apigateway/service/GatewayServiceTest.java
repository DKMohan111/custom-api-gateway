package com.apigateway.service;

import com.apigateway.entity.Route;
import com.apigateway.exception.RouteNotFoundException;
import com.apigateway.repository.RouteRepository;
import com.apigateway.security.TargetUrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

    private RouteService routeService;
    private GatewayService gatewayService;
    private MockRestServiceServer mockServer;

    @Mock
    private TargetUrlValidator targetUrlValidator;

    @BeforeEach
    void setUp() {
        routeService = mock(RouteService.class);

        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        gatewayService = new GatewayService(
                routeService,
                builder,
                targetUrlValidator
        );
    }

    @Test
    void forwardRequest_forwardsGetRequestAndReturnsResponse() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products/42"))
                .thenReturn(route);
        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products/42"))
                .andExpect(method(
                        org.springframework.http.HttpMethod.GET))
                .andExpect(header(
                        "X-Correlation-ID", "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"id\":42}",
                        MediaType.APPLICATION_JSON));

        var response = gatewayService.forwardRequest(
                "/products/42", "test-correlation-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":42}", response.getBody());

        mockServer.verify();
    }
    @Test
    void forwardRequest_forwardsPostRequestAndReturnsResponse() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products/42"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products/42"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header(
                        "X-Correlation-ID", "test-correlation-id"))
                .andExpect(content().json("{\"name\":\"Laptop\"}"))
                .andRespond(withSuccess(
                        "{\"id\":42,\"name\":\"Laptop\"}",
                        MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = gatewayService.forwardPostRequest(
                "/products/42",
                "{\"name\":\"Laptop\"}",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "{\"id\":42,\"name\":\"Laptop\"}",
                response.getBody()
        );

        mockServer.verify();
    }

    @Test
    void forwardRequest_forwardsPutRequestAndReturnsResponse() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products/42"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products/42"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(
                        "X-Correlation-ID", "test-correlation-id"))
                .andExpect(content().json("{\"name\":\"Updated Laptop\"}"))
                .andRespond(withSuccess(
                        "{\"id\":42,\"name\":\"Updated Laptop\"}",
                        MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = gatewayService.forwardPutRequest(
                "/products/42",
                "{\"name\":\"Updated Laptop\"}",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "{\"id\":42,\"name\":\"Updated Laptop\"}",
                response.getBody()
        );

        mockServer.verify();
    }
    @Test
    void forwardRequest_forwardsDeleteRequestAndReturnsResponse() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products/42"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products/42"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(
                        "X-Correlation-ID", "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"deleted\":true}",
                        MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");

        var response = gatewayService.forwardDeleteRequest(
                "/products/42",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "{\"deleted\":true}",
                response.getBody()
        );

        mockServer.verify();
    }
    @Test
    void forwardRequest_forwardsPatchRequestAndReturnsResponse() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products/42"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products/42"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(
                        "X-Correlation-ID", "test-correlation-id"))
                .andExpect(content().json("{\"name\":\"Updated Laptop\"}"))
                .andRespond(withSuccess(
                        "{\"id\":42,\"name\":\"Updated Laptop\"}",
                        MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = gatewayService.forwardPatchRequest(
                "/products/42",
                "{\"name\":\"Updated Laptop\"}",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "{\"id\":42,\"name\":\"Updated Laptop\"}",
                response.getBody()
        );

        mockServer.verify();
    }
    @Test
    void forwardRequest_withQueryParams_forwardsCorrectUrl() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products?page=1&limit=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[]", MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = gatewayService.forwardRequest(
                "/products?page=1&limit=10",
                "test-correlation-id"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[]", response.getBody());

        mockServer.verify();
    }

    @Test
    void forwardRequest_whenRouteDoesNotExist_throwsException() {
        when(routeService.findRouteForPath("/unknown"))
                .thenThrow(new RouteNotFoundException("Route not found"));

        assertThrows(
                RouteNotFoundException.class,
                () -> gatewayService.forwardRequest(
                        "/unknown",
                        "test-correlation-id"
                )
        );
    }
    @Test
    void forwardPostRequest_withQueryParams_forwardsCorrectUrl() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        mockServer.expect(requestTo(
                        "http://localhost:8081/products?page=1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"name\":\"Phone\"}"))
                .andRespond(withSuccess(
                        "{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = gatewayService.forwardPostRequest(
                "/products?page=1",
                "{\"name\":\"Phone\"}",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\":1}", response.getBody());

        mockServer.verify();
    }
    @Test
    void forwardPutRequest_withQueryParams_forwardsCorrectUrl() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        mockServer.expect(requestTo(
                        "http://localhost:8081/products?page=1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json("{\"name\":\"Updated Phone\"}"))
                .andRespond(withSuccess(
                        "{\"id\":1,\"name\":\"Updated Phone\"}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = gatewayService.forwardPutRequest(
                "/products?page=1",
                "{\"name\":\"Updated Phone\"}",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "{\"id\":1,\"name\":\"Updated Phone\"}",
                response.getBody()
        );

        mockServer.verify();
    }
    @Test
    void forwardPatchRequest_withQueryParams_forwardsCorrectUrl() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        mockServer.expect(requestTo(
                        "http://localhost:8081/products?page=1"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().json("{\"name\":\"Updated Phone\"}"))
                .andRespond(withSuccess(
                        "{\"id\":1,\"name\":\"Updated Phone\"}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = gatewayService.forwardPatchRequest(
                "/products?page=1",
                "{\"name\":\"Updated Phone\"}",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "{\"id\":1,\"name\":\"Updated Phone\"}",
                response.getBody()
        );

        mockServer.verify();
    }
    @Test
    void forwardDeleteRequest_withQueryParams_forwardsCorrectUrl() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();

        mockServer.expect(requestTo(
                        "http://localhost:8081/products?page=1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess(
                        "{\"message\":\"deleted\"}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = gatewayService.forwardDeleteRequest(
                "/products?page=1",
                headers
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\":\"deleted\"}", response.getBody());

        mockServer.verify();
    }
    @Test
    void forwardRequest_forwardsResponseHeaders() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON)
                                .header("X-Test-Header", "test-value")
                );

        ResponseEntity<String> response =
                gatewayService.forwardRequest(
                        "/products",
                        "test-correlation-id"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test-value",
                response.getHeaders().getFirst("X-Test-Header"));

        mockServer.verify();
    }
    @Test
    void forwardRequest_doesNotForwardHopByHopResponseHeaders() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON)
                                .header("Connection", "keep-alive")
                                .header("Transfer-Encoding", "chunked")
                );

        ResponseEntity<String> response =
                gatewayService.forwardRequest(
                        "/products",
                        "test-correlation-id"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getFirst("Connection"));
        assertNull(response.getHeaders().getFirst("Transfer-Encoding"));

        mockServer.verify();
    }
    @Test
    void forwardRequest_forwardsCorrelationId() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "X-Correlation-ID",
                        "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"id\":1}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response =
                gatewayService.forwardRequest(
                        "/products",
                        "test-correlation-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        mockServer.verify();
    }
    @Test
    void forwardPostRequest_forwardsCorrelationId() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");
        headers.setContentType(MediaType.APPLICATION_JSON);

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "X-Correlation-ID",
                        "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"id\":1}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response =
                gatewayService.forwardPostRequest(
                        "/products",
                        "{\"name\":\"Phone\"}",
                        headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        mockServer.verify();
    }
    @Test
    void forwardPutRequest_forwardsCorrelationId() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");
        headers.setContentType(MediaType.APPLICATION_JSON);

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(
                        "X-Correlation-ID",
                        "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"id\":1}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response =
                gatewayService.forwardPutRequest(
                        "/products",
                        "{\"name\":\"Updated Phone\"}",
                        headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        mockServer.verify();
    }
    @Test
    void forwardPatchRequest_forwardsCorrelationId() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");
        headers.setContentType(MediaType.APPLICATION_JSON);

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(
                        "X-Correlation-ID",
                        "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"id\":1}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response =
                gatewayService.forwardPatchRequest(
                        "/products",
                        "{\"name\":\"Updated Phone\"}",
                        headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        mockServer.verify();
    }
    @Test
    void forwardDeleteRequest_forwardsCorrelationId() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-correlation-id");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(
                        "X-Correlation-ID",
                        "test-correlation-id"))
                .andRespond(withSuccess(
                        "{\"message\":\"deleted\"}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity<String> response =
                gatewayService.forwardDeleteRequest(
                        "/products",
                        headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        mockServer.verify();
    }
    @Test
    void forwardRequest_whenBackendReturns500_returns500Response() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);

        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo(
                        "http://localhost:8081/products"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withServerError()
                                .body("{\"error\":\"Backend failure\"}")
                );

        ResponseEntity<String> response =
                gatewayService.forwardRequest(
                        "/products",
                        "test-correlation-id");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());

        assertEquals(
                "{\"error\":\"Backend failure\"}",
                response.getBody()
        );

        mockServer.verify();
    }
    @Test
    void forwardRequest_whenBackendConnectionFails_throwsResourceAccessException() {
        Route route = mock(Route.class);

        when(routeService.findRouteForPath("/products"))
                .thenReturn(route);
        when(route.getPath()).thenReturn("/products");
        when(route.getTargetUrl())
                .thenReturn("http://localhost:8081");

        mockServer.expect(requestTo("http://localhost:8081/products"))
                .andRespond(withException(
                        new java.io.IOException("Connection refused")
                ));

        assertThrows(
                ResourceAccessException.class,
                () -> gatewayService.forwardRequest(
                        "/products",
                        "test-correlation-id"
                )
        );

        mockServer.verify();
    }
}