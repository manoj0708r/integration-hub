package com.aspire.poc.integration_hub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI integrationHubOpenAPI() {

        return new OpenAPI().info(new Info().title("Integration Hub - Order Management API")
                .description("""
                REST APIs for managing customer orders.
                
                This API supports order creation with
                idempotency handling and order retrieval.
                """).version("1.0.0").contact(new Contact().name("Integration Hub Development Team")
                        .email("helpdesk@aspiresys.com")).license(new License().name("Internal API")));
    }
}
