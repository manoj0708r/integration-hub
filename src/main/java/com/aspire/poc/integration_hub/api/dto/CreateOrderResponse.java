package com.aspire.poc.integration_hub.api.dto;

public record CreateOrderResponse(String orderId, String status, String correlationId) {
}
