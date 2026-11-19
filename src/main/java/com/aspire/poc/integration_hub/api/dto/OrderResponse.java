package com.aspire.poc.integration_hub.api.dto;

import java.util.List;

public record OrderResponse(

        String orderId,

        String customerId,

        String status,

        List<AuditResponse> auditTrail
) {
}
