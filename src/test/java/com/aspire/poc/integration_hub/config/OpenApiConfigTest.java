package com.aspire.poc.integration_hub.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig config =
            new OpenApiConfig();

    @Test
    void shouldBuildOpenApiDefinition() {

        OpenAPI openAPI =
                config.integrationHubOpenAPI();

        assertNotNull(openAPI);

        assertEquals(
                "Integration Hub - Order Management API",
                openAPI.getInfo().getTitle()
        );

        assertEquals(
                "1.0.0",
                openAPI.getInfo().getVersion()
        );

        assertEquals(
                "Integration Hub Development Team",
                openAPI.getInfo().getContact().getName()
        );

        assertEquals(
                "helpdesk@aspiresys.com",
                openAPI.getInfo().getContact().getEmail()
        );
    }
}