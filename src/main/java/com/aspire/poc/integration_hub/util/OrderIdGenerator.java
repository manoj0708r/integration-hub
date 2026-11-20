package com.aspire.poc.integration_hub.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderIdGenerator {

    public String nextId() {

        return "ORD-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}