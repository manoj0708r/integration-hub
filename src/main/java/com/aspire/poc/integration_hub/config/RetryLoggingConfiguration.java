package com.aspire.poc.integration_hub.config;

import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RetryLoggingConfiguration {

    private final RetryRegistry retryRegistry;

    @PostConstruct
    public void configure() {

        retryRegistry.retry("payment")
                .getEventPublisher()
                .onRetry(event ->
                        System.out.println(
                                "Retry Attempt : "
                                        + event.getNumberOfRetryAttempts()
                        )
                );
    }
}