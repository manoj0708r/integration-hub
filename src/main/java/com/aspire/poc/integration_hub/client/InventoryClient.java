package com.aspire.poc.integration_hub.client;

import com.aspire.poc.integration_hub.client.dto.InventoryRequest;
import com.aspire.poc.integration_hub.client.dto.InventoryResponse;
import com.aspire.poc.integration_hub.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final RestClient restClient;

    private final ApplicationProperties properties;

    public InventoryResponse reserve(InventoryRequest request) {

        return restClient.post().uri(properties.getInventoryUrl() + "/inventory/reserve").body(request).retrieve().body(InventoryResponse.class);
    }

    public void release(InventoryRequest request) {

        restClient.post().uri(properties.getInventoryUrl() + "/inventory/release").body(request).retrieve().toBodilessEntity();
    }
}
