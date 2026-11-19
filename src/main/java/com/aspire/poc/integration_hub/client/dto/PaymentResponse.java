package com.aspire.poc.integration_hub.client.dto;

public record PaymentResponse(

        boolean authorised,

        String transactionId) {
}

