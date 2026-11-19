package com.aspire.poc.integration_hub.api.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("Order not found : " + orderId);
    }
}