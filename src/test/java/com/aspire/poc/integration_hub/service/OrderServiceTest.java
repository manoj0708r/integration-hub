package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.api.dto.CreateOrderRequest;
import com.aspire.poc.integration_hub.api.dto.CreateOrderResponse;
import com.aspire.poc.integration_hub.api.dto.OrderItemRequest;
import com.aspire.poc.integration_hub.api.dto.OrderResponse;
import com.aspire.poc.integration_hub.api.exception.OrderNotFoundException;
import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.repository.OrderAuditRepository;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.impl.OrderOrchestrationService;
import com.aspire.poc.integration_hub.util.OrderIdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderAuditRepository auditRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private AuditService auditService;

    @Mock
    private OrderIdGenerator orderIdGenerator;

    @Mock
    private OrderOrchestrationService orderOrchestrationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldReturnExistingOrderForDuplicateRequest() {

        Order existingOrder = Order.builder()
                .orderId("ORD-1001")
                .correlationId("CORR-001")
                .status(OrderStatus.SHIPPED)
                .build();

        when(idempotencyService.findByKey("IDEMP-1"))
                .thenReturn(Optional.of("ORD-1001"));

        when(orderRepository.findById("ORD-1001"))
                .thenReturn(Optional.of(existingOrder));

        CreateOrderResponse response =
                orderService.createOrder(
                        buildRequest(),
                        "IDEMP-1",
                        "CORR-001"
                );

        assertEquals("ORD-1001", response.orderId());

        verify(orderOrchestrationService, never())
                .process(any(), any());
    }

    @Test
    void shouldCreateNewOrder() {

        when(idempotencyService.findByKey(anyString()))
                .thenReturn(Optional.empty());

        when(orderIdGenerator.nextId())
                .thenReturn("ORD-2001");

        Order persisted = Order.builder()
                .orderId("ORD-2001")
                .customerId("CUST-1001")
                .correlationId("CORR-001")
                .status(OrderStatus.RECEIVED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById("ORD-2001"))
                .thenReturn(Optional.of(persisted));

        CreateOrderResponse response =
                orderService.createOrder(
                        buildRequest(),
                        "IDEMP-NEW",
                        "CORR-001"
                );

        assertEquals("ORD-2001", response.orderId());

        verify(orderRepository)
                .save(any(Order.class));

        verify(idempotencyService)
                .save("IDEMP-NEW", "ORD-2001");

        verify(orderOrchestrationService)
                .process(any(), any());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {

        when(orderRepository.findById("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder("UNKNOWN")
        );
    }

    private CreateOrderRequest buildRequest() {

        return new CreateOrderRequest(
                "CUST-1001",
                List.of(
                        new OrderItemRequest(
                                "SKU-IPHONE-16",
                                1,
                                BigDecimal.valueOf(85000)
                        )
                ),
                BigDecimal.valueOf(85000),
                "INR"
        );
    }
}