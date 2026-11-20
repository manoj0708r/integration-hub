
package com.aspire.poc.integration_hub.client;

import com.aspire.poc.integration_hub.client.dto.InventoryRequest;
import com.aspire.poc.integration_hub.client.dto.InventoryResponse;
import com.aspire.poc.integration_hub.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final RestClient restClient;

    private final ApplicationProperties properties;

    public InventoryResponse reserve(InventoryRequest request) {

        log.info("Requesting inventory reservation. orderId={}",
                request.orderId());

        InventoryResponse response = restClient.post()
                .uri(properties.getInventoryUrl() + "/inventory/reserve")
                .body(request)
                .retrieve()
                .body(InventoryResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Inventory service returned an empty response"
            );
        }

        log.info(
                "Inventory reservation response. orderId={}, reserved={}",
                request.orderId(),
                response.reserved()
        );

        return response;
    }

    public void release(InventoryRequest request) {

        log.info("Requesting inventory release. orderId={}",
                request.orderId());

        restClient.post()
                .uri(properties.getInventoryUrl() + "/inventory/release")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Inventory release completed. orderId={}",
                request.orderId());
    }
}
