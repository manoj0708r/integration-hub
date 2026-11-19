package com.aspire.poc.integration_hub.messaging.dto;

import java.io.Serializable;

public record ShipmentRequestedEvent(

        String orderId,

        String customerId,

        String correlationId) implements Serializable {
}
