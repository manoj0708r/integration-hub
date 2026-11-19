package com.aspire.poc.integration_hub.client;

import com.aspire.poc.integration_hub.client.dto.PaymentRequest;
import com.aspire.poc.integration_hub.client.dto.PaymentResponse;
import com.aspire.poc.integration_hub.config.ApplicationProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PaymentClient {

    private final RestClient restClient;

    private final ApplicationProperties properties;

    @Retry(name = "payment", fallbackMethod = "paymentFallback")
    @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
    public PaymentResponse authorise(PaymentRequest request) {

        return restClient.post().uri(properties.getPaymentUrl() + "/payments/authorise").body(request).retrieve().body(PaymentResponse.class);
    }

    public PaymentResponse paymentFallback(PaymentRequest request, Exception exception) {

        System.err.println("Payment fallback invoked : " + exception.getMessage());

        return new PaymentResponse(false, null);
    }
}