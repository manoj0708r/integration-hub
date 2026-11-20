package com.aspire.poc.integration_hub.client;

import com.aspire.poc.integration_hub.client.dto.PaymentRequest;
import com.aspire.poc.integration_hub.client.dto.PaymentResponse;
import com.aspire.poc.integration_hub.config.ApplicationProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentClient {

    private final RestClient restClient;

    private final ApplicationProperties properties;

    @Retry(name = "payment", fallbackMethod = "paymentFallback")
    @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
    public PaymentResponse authorise(PaymentRequest request) {

        log.info("Sending payment authorization request. orderId={}",
                request.orderId());

        PaymentResponse response = restClient.post()
                .uri(properties.getPaymentUrl() + "/payments/authorise")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Payment service returned an empty response"
            );
        }

        log.info(
                "Payment authorization response received. orderId={}, authorised={}",
                request.orderId(),
                response.authorised()
        );

        return response;
    }

    public PaymentResponse paymentFallback(
            PaymentRequest request,
            Exception exception) {

        log.error(
                "Payment service failed after resilience handling. orderId={}",
                request.orderId(),
                exception
        );

        // Preserve the original technical exception.
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        throw new IllegalStateException(
                "Payment authorization could not be completed",
                exception
        );
    }
}
