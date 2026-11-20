package com.aspire.poc.integration_hub.exporter;

import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderExporterTest {

    private final OrderExporter exporter =
            new OrderExporter();

    @Test
    void shouldExportOrdersToCsv() throws Exception {

        Order order = Order.builder()
                .orderId("ORD-1001")
                .customerId("CUST-1001")
                .status(OrderStatus.SHIPPED)
                .correlationId("CORR-001")
                .build();

        Path file =
                exporter.export(List.of(order));

        assertTrue(
                Files.exists(file)
        );

        String content =
                Files.readString(file);

        assertTrue(
                content.contains("ORD-1001")
        );

        assertTrue(
                content.contains("customerId")
        );
    }
}