package com.aspire.poc.integration_hub.messaging.dto;

import java.io.Serializable;

public record ShipmentStatusEvent(

        String orderId,

        String status) implements Serializable {
}