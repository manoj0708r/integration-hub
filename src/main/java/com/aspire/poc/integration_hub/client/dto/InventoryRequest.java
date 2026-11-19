package com.aspire.poc.integration_hub.client.dto;

import com.aspire.poc.integration_hub.api.dto.OrderItemRequest;

import java.util.List;

public record InventoryRequest(

        String orderId,

        List<OrderItemRequest> items) {
}
