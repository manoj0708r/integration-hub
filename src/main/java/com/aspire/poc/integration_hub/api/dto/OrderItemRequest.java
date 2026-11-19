package com.aspire.poc.integration_hub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(

        @NotBlank String sku,

        @Positive Integer quantity,

        @Positive BigDecimal unitPrice) {
}
