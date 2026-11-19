package com.aspire.poc.integration_hub.exporter;

import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderExportScheduler {

    private final OrderRepository orderRepository;

    private final OrderExporter orderExporter;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void exportOrders() {

        List<Order> orders = orderRepository.findByExportedFalse();

        if (orders.isEmpty()) {

            return;
        }

        try {

            Path filePath = orderExporter.export(orders);

            for (Order order : orders) {

                order.setExported(true);
            }

            orderRepository.saveAll(orders);

            System.out.println("Exported " + orders.size() + " orders to " + filePath);

        } catch (Exception exception) {

            System.err.println("CSV Export Failed : " + exception.getMessage());
        }
    }
}
