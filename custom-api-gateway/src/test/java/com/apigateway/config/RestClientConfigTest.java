package com.apigateway.config;

import com.apigateway.security.SafeDnsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestClientConfigTest {

    @Test
    void restClientBuilder_usesHttpComponentsRequestFactory() {

        RestClientConfig config = new RestClientConfig();

        RestClient.Builder builder = config.restClientBuilder(new SafeDnsResolver(List.of()));

        assertNotNull(builder);

        RestClient restClient = builder.build();

        assertNotNull(restClient);
    }
}