package com.aspire.poc.integration_hub.api.dto;

import com.aspire.poc.integration_hub.domain.enums.OrderStatus;

import java.util.List;

public record OrderDetailsResponse(

        String orderId,

        OrderStatus status,

        List<AuditResponse> audit

) {
}
