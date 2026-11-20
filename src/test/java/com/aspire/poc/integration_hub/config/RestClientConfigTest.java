package com.aspire.poc.integration_hub.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

class RestClientConfigTest {

    private final RestClientConfig config =
            new RestClientConfig();

    @Test
    void shouldCreateRestClient() {

        RestClient client =
                config.restClient();

        assertNotNull(client);
    }
}