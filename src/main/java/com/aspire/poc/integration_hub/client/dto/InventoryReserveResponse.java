package com.aspire.poc.integration_hub.client.dto;

public record InventoryReserveResponse(

        boolean reserved,

        String reason) {
}
