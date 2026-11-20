package com.aspire.poc.integration_hub.exporter;

import com.aspire.poc.integration_hub.domain.entity.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class OrderExporter {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    public Path export(List<Order> orders) throws IOException {

        Path exportDirectory = Paths.get("exports");

        Files.createDirectories(exportDirectory);

        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);

        // UUID ensures a unique filename even when exports run close together.
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        String fileName = "orders-" + timestamp + "-" + uniqueId + ".csv";

        Path filePath = exportDirectory.resolve(fileName);

        // CREATE_NEW prevents accidental overwriting of an existing file.
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {

            writer.write("orderId,customerId,status,correlationId");
            writer.newLine();

            for (Order order : orders) {

                writer.write(escapeCsv(order.getOrderId()) + "," + escapeCsv(order.getCustomerId()) + "," + escapeCsv(order.getStatus()) + "," + escapeCsv(order.getCorrelationId()));

                writer.newLine();
            }
        }

        return filePath;
    }

    private String escapeCsv(Object value) {

        if (value == null) {
            return "";
        }

        String text = value.toString();

        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {

            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}
