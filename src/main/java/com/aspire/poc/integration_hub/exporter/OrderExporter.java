package com.aspire.poc.integration_hub.exporter;

import com.aspire.poc.integration_hub.domain.entity.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderExporter {

    public Path export(
            List<Order> orders)
            throws IOException {

        Path exportDirectory =
                Paths.get("exports");

        Files.createDirectories(
                exportDirectory
        );

        String timestamp =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmss"
                                )
                        );

        Path filePath =
                exportDirectory.resolve(
                        "orders-" +
                                timestamp +
                                ".csv"
                );

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             filePath
                     )) {

            writer.write(
                    "orderId,customerId,status,correlationId"
            );

            writer.newLine();

            for (Order order : orders) {

                writer.write(
                        order.getOrderId()
                                + ","
                                + order.getCustomerId()
                                + ","
                                + order.getStatus()
                                + ","
                                + order.getCorrelationId()
                );

                writer.newLine();
            }
        }

        return filePath;
    }
}