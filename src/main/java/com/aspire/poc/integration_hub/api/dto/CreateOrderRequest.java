package com.aspire.poc.integration_hub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(

        @NotBlank String customerId,

        @NotEmpty List<OrderItemRequest> items,

        @Positive BigDecimal totalAmount,

        @NotBlank String currency) {
}
