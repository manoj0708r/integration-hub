package com.aspire.poc.integration_hub.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderNumberGenerator {

    private final AtomicInteger sequence = new AtomicInteger(1);

    public String generate() {

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        return "ORD-" + date + "-" + String.format("%04d", sequence.getAndIncrement());
    }
}