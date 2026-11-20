package com.aspire.poc.integration_hub.exporter;

import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExportScheduler {

    private final OrderRepository orderRepository;

    private final OrderExporter orderExporter;

    // Execute CSV export every 2 minutes.
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void exportOrders() {

        List<Order> orders = orderRepository.findByExportedFalse();

        if (orders.isEmpty()) {
            log.info("No pending orders available for CSV export");
            return;
        }

        try {

            // Step 1: Generate CSV file.
            Path filePath = orderExporter.export(orders);

            // Step 2: Mark orders as exported only after successful file creation.
            for (Order order : orders) {
                order.setExported(true);
            }

            // Step 3: Persist exported status.
            orderRepository.saveAll(orders);

            log.info("Successfully exported {} orders to {}", orders.size(), filePath);

        } catch (Exception exception) {

            log.error("CSV export failed. {} orders remain eligible for retry.", orders.size(), exception);
        }
    }
}
