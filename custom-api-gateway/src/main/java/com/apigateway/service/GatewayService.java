
package com.apigateway.service;

import com.apigateway.entity.Route;
import com.apigateway.security.TargetUrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GatewayService {

    private final RouteService routeService;
    private final RestClient restClient;
    private final TargetUrlValidator targetUrlValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(GatewayService.class);

    public GatewayService(
            RouteService routeService,
            RestClient.Builder restClientBuilder,
            TargetUrlValidator targetUrlValidator) {
        this.routeService = routeService;
        this.restClient = restClientBuilder.build();
        this.targetUrlValidator = targetUrlValidator;
    }

    public ResponseEntity<String> forwardRequest(
            String path, String correlationId) {

        String routePath = path;
        int queryIndex = path.indexOf("?");

        if (queryIndex != -1) {
            routePath = path.substring(0, queryIndex);
        }

        Route route = routeService.findRouteForPath(routePath);

        String suffix = routePath.substring(route.getPath().length());

        String targetUrl = route.getTargetUrl() + routePath;

        if (queryIndex != -1) {
            targetUrl += path.substring(queryIndex);
        }
        targetUrlValidator.validate(targetUrl);

        final String requestCorrelationId = correlationId;
        final String requestTargetUrl = targetUrl;

        return restClient.get()
                .uri(targetUrl)
                .header("X-Correlation-ID", correlationId)
                .exchange((request, response) -> {

                    logger.info(
                            "Gateway request completed: correlationId={}, targetUrl={}, status={}",
                            requestCorrelationId,
                            requestTargetUrl,
                            response.getStatusCode().value()
                    );
                    String body =
                            new String(response.getBody().readAllBytes());

                    return buildResponse(
                            response.getStatusCode(),
                            response.getHeaders(),
                            body
                    );
                });
    }


    public ResponseEntity<String> forwardPostRequest(
            String path,
            String requestBody,
            HttpHeaders headers) {

        String routePath = path;
        int queryIndex = path.indexOf("?");

        if (queryIndex != -1) {
            routePath = path.substring(0, queryIndex);
        }

        Route route = routeService.findRouteForPath(routePath);

        String suffix = routePath.substring(route.getPath().length());

        String targetUrl = route.getTargetUrl() + routePath;

        if (queryIndex != -1) {
            targetUrl += path.substring(queryIndex);
        }
        targetUrlValidator.validate(targetUrl);

        return restClient.post()
                .uri(targetUrl)
                .headers(outgoingHeaders ->
                        copyHeaders(headers, outgoingHeaders))
                .body(requestBody)
                .exchange((request, response) -> {
                    String body =
                            new String(response.getBody().readAllBytes());

                    return buildResponse(
                            response.getStatusCode(),
                            response.getHeaders(),
                            body
                    );
                });
    }


    public ResponseEntity<String> forwardPutRequest(
            String path,
            String requestBody,
            HttpHeaders headers) {

        String routePath = path;
        int queryIndex = path.indexOf("?");

        if (queryIndex != -1) {
            routePath = path.substring(0, queryIndex);
        }

        Route route = routeService.findRouteForPath(routePath);

        String suffix = routePath.substring(route.getPath().length());

        String targetUrl = route.getTargetUrl() + routePath;

        if (queryIndex != -1) {
            targetUrl += path.substring(queryIndex);
        }

        targetUrlValidator.validate(targetUrl);
        return restClient.put()
                .uri(targetUrl)
                .headers(outgoingHeaders ->
                        copyHeaders(headers, outgoingHeaders))
                .body(requestBody)
                .exchange((request, response) -> {
                    String body =
                            new String(response.getBody().readAllBytes());

                    return buildResponse(
                            response.getStatusCode(),
                            response.getHeaders(),
                            body
                    );
                });
    }

    public ResponseEntity<String> forwardDeleteRequest(
            String path,
            HttpHeaders headers) {

        String routePath = path;
        int queryIndex = path.indexOf("?");

        if (queryIndex != -1) {
            routePath = path.substring(0, queryIndex);
        }

        Route route = routeService.findRouteForPath(routePath);

        String suffix = routePath.substring(route.getPath().length());
        String targetUrl = route.getTargetUrl() + routePath;

        if (queryIndex != -1) {
            targetUrl += path.substring(queryIndex);
        }

        targetUrlValidator.validate(targetUrl);

        return restClient.delete()
                .uri(targetUrl)
                .headers(outgoingHeaders ->
                        copyHeaders(headers, outgoingHeaders))
                .exchange((request, response) -> {

                    String body =
                            new String(response.getBody().readAllBytes());

                    return buildResponse(
                            response.getStatusCode(),
                            response.getHeaders(),
                            body
                    );
                });
    }

    private ResponseEntity<String> buildResponse(
            org.springframework.http.HttpStatusCode status,
            HttpHeaders headers,
            String body) {

        HttpHeaders responseHeaders = new HttpHeaders();

        headers.forEach((name, values) -> {
            if (!name.equalsIgnoreCase("Transfer-Encoding")
                    && !name.equalsIgnoreCase("Content-Length")
                    && !name.equalsIgnoreCase("Connection")) {
                responseHeaders.put(name, values);
            }
        });

        return ResponseEntity.status(status)
                .headers(responseHeaders)
                .body(body);
    }


    private void copyHeaders(
            HttpHeaders source,
            HttpHeaders target) {

        source.forEach((name, values) -> {
            if (!name.equalsIgnoreCase("Host")
                    && !name.equalsIgnoreCase("Content-Length")
                    && !name.equalsIgnoreCase("Connection")
                    && !name.equalsIgnoreCase("Transfer-Encoding")
                    && !name.equalsIgnoreCase("Authorization")
                    && !name.equalsIgnoreCase("Cookie")) {
                target.put(name, values);
            }
        });
    }


    public ResponseEntity<String> forwardPatchRequest(
            String path,
            String requestBody,
            HttpHeaders headers) {

        String routePath = path;
        int queryIndex = path.indexOf("?");

        if (queryIndex != -1) {
            routePath = path.substring(0, queryIndex);
        }

        Route route = routeService.findRouteForPath(routePath);

        String suffix = routePath.substring(route.getPath().length());

        String targetUrl = route.getTargetUrl() + routePath;

        if (queryIndex != -1) {
            targetUrl += path.substring(queryIndex);
        }
        targetUrlValidator.validate(targetUrl);

        return restClient.patch()
                .uri(targetUrl)
                .headers(outgoingHeaders ->
                        copyHeaders(headers, outgoingHeaders))
                .body(requestBody)
                .exchange((request, response) -> {
                    String body =
                            new String(response.getBody().readAllBytes());

                    return buildResponse(
                            response.getStatusCode(),
                            response.getHeaders(),
                            body
                    );
                });
    }

}
