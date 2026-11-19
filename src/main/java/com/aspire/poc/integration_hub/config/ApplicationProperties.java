package com.aspire.poc.integration_hub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

@ConfigurationProperties(prefix = "hub")
@Component
@Getter
@Setter
public class ApplicationProperties {

    private String inventoryUrl;

    private String paymentUrl;

    private String apiKey;
}
