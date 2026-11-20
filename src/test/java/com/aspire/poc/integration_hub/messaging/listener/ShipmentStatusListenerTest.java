package com.aspire.poc.integration_hub.messaging.listener;

import com.aspire.poc.integration_hub.api.exception.OrderNotFoundException;
import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.messaging.dto.ShipmentStatusEvent;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentStatusListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ShipmentStatusListener shipmentStatusListener;

    private Order order;

    @BeforeEach
    void setUp() {

        order = Order.builder()
                .orderId("ORD-1001")
                .customerId("CUST-1001")
                .correlationId("CORR-001")
                .status(OrderStatus.SHIPMENT_REQUESTED)
                .build();
    }

    @Test
    void shouldMarkOrderAsShipped() {

        ShipmentStatusEvent event =
                new ShipmentStatusEvent(
                        "ORD-1001",
                        "SHIPPED"
                );

        when(orderRepository.findById("ORD-1001"))
                .thenReturn(Optional.of(order));

        shipmentStatusListener.handleShipmentStatus(event);

        assertEquals(
                OrderStatus.SHIPPED,
                order.getStatus()
        );

        verify(orderRepository)
                .save(order);

        verify(auditService)
                .audit(
                        "ORD-1001",
                        "SHIPMENT",
                        "SHIPPED",
                        "CORR-001"
                );
    }

    @Test
    void shouldIgnoreDuplicateShipmentStatus() {

        order.setStatus(OrderStatus.SHIPPED);

        ShipmentStatusEvent event =
                new ShipmentStatusEvent(
                        "ORD-1001",
                        "SHIPPED"
                );

        when(orderRepository.findById("ORD-1001"))
                .thenReturn(Optional.of(order));

        shipmentStatusListener.handleShipmentStatus(event);

        verify(orderRepository, never())
                .save(any());

        verify(auditService, never())
                .audit(any(), any(), any(), any());
    }

    @Test
    void shouldIgnoreUnsupportedShipmentStatus() {

        ShipmentStatusEvent event =
                new ShipmentStatusEvent(
                        "ORD-1001",
                        "FAILED"
                );

        when(orderRepository.findById("ORD-1001"))
                .thenReturn(Optional.of(order));

        shipmentStatusListener.handleShipmentStatus(event);

        assertEquals(
                OrderStatus.SHIPMENT_REQUESTED,
                order.getStatus()
        );

        verify(orderRepository, never())
                .save(any());

        verify(auditService, never())
                .audit(any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {

        ShipmentStatusEvent event =
                new ShipmentStatusEvent(
                        "ORD-9999",
                        "SHIPPED"
                );

        when(orderRepository.findById("ORD-9999"))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> shipmentStatusListener.handleShipmentStatus(event)
        );
    }

    @Test
    void shouldIgnoreUnexpectedOrderStatus() {

        order.setStatus(OrderStatus.PAYMENT_AUTHORISED);

        ShipmentStatusEvent event =
                new ShipmentStatusEvent(
                        "ORD-1001",
                        "SHIPPED"
                );

        when(orderRepository.findById("ORD-1001"))
                .thenReturn(Optional.of(order));

        shipmentStatusListener.handleShipmentStatus(event);

        verify(orderRepository, never())
                .save(any());

        verify(auditService, never())
                .audit(any(), any(), any(), any());
    }
}