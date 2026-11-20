package com.aspire.poc.integration_hub.exporter;

import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderExportSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderExporter orderExporter;

    @InjectMocks
    private OrderExportScheduler scheduler;

    @Test
    void shouldSkipWhenNoOrdersFound() throws IOException {

        when(orderRepository.findByExportedFalse())
                .thenReturn(List.of());

        scheduler.exportOrders();

        verify(orderExporter, never())
                .export(anyList());
    }

    @Test
    void shouldExportOrders() throws Exception {

        Order order = new Order();

        when(orderRepository.findByExportedFalse())
                .thenReturn(List.of(order));

        when(orderExporter.export(anyList()))
                .thenReturn(Path.of("orders.csv"));

        scheduler.exportOrders();

        verify(orderRepository)
                .saveAll(anyList());
    }

    @Test
    void shouldHandleExportFailure() throws Exception {

        Order order = new Order();

        when(orderRepository.findByExportedFalse())
                .thenReturn(List.of(order));

        when(orderExporter.export(anyList()))
                .thenThrow(new RuntimeException("failure"));

        scheduler.exportOrders();

        verify(orderRepository, never())
                .saveAll(anyList());
    }
}