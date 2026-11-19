package com.aspire.poc.integration_hub.api.dto;

import java.time.LocalDateTime;

public record AuditResponse(

        String step,

        String outcome,

        LocalDateTime timestamp

) {
}
