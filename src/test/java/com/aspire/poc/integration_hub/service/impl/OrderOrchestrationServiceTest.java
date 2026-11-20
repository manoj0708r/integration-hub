
package com.aspire.poc.integration_hub.service.impl;

import com.aspire.poc.integration_hub.api.dto.CreateOrderRequest;
import com.aspire.poc.integration_hub.api.dto.OrderItemRequest;
import com.aspire.poc.integration_hub.client.InventoryClient;
import com.aspire.poc.integration_hub.client.PaymentClient;
import com.aspire.poc.integration_hub.client.dto.InventoryResponse;
import com.aspire.poc.integration_hub.client.dto.PaymentResponse;
import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import com.aspire.poc.integration_hub.messaging.publisher.ShipmentPublisher;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderOrchestrationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private AuditService auditService;

    @Mock
    private ShipmentPublisher shipmentPublisher;

    @InjectMocks
    private OrderOrchestrationService orderOrchestrationService;

    private Order order;

    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {

        order = Order.builder()
                .orderId("ORD-1001")
                .customerId("CUST-1001")
                .correlationId("CORR-001")
                .status(OrderStatus.RECEIVED)
                .totalAmount(BigDecimal.valueOf(85000))
                .currency("INR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = new CreateOrderRequest(
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

    @Test
    void shouldRejectOrderWhenInventoryReservationFails() {

        // Arrange
        when(inventoryClient.reserve(any()))
                .thenReturn(
                        new InventoryResponse(
                                false,
                                "Insufficient Stock"
                        )
                );

        // Act
        orderOrchestrationService.process(order, request);

        // Assert: order status
        assertEquals(
                OrderStatus.REJECTED,
                order.getStatus()
        );

        // Verify inventory reservation was attempted
        verify(inventoryClient, times(1))
                .reserve(any());

        // Payment should not be attempted
        verify(paymentClient, never())
                .authorise(any());

        // Inventory release should not be attempted
        verify(inventoryClient, never())
                .release(any());

        // Shipment should not be published
        verify(shipmentPublisher, never())
                .publish(any());

        // Verify order was saved
        verify(orderRepository, times(1))
                .save(order);

        // Verify inventory failure audit
        verify(auditService).audit(
                "ORD-1001",
                "INVENTORY",
                "REJECTED",
                "CORR-001"
        );
    }

    @Test
    void shouldReleaseInventoryWhenPaymentFails() {

        // Arrange
        when(inventoryClient.reserve(any()))
                .thenReturn(
                        new InventoryResponse(
                                true,
                                null
                        )
                );

        when(paymentClient.authorise(any()))
                .thenReturn(
                        new PaymentResponse(
                                false,
                                null
                        )
                );

        // Act
        orderOrchestrationService.process(order, request);

        // Assert: order status
        assertEquals(
                OrderStatus.PAYMENT_FAILED,
                order.getStatus()
        );

        // Verify inventory reservation
        verify(inventoryClient, times(1))
                .reserve(any());

        // Verify payment authorization
        verify(paymentClient, times(1))
                .authorise(any());

        // Verify inventory compensation
        verify(inventoryClient, times(1))
                .release(any());

        // Shipment should not be published
        verify(shipmentPublisher, never())
                .publish(any());

        // Verify order was saved after reservation and payment failure
        verify(orderRepository, times(2))
                .save(order);

        // Verify inventory reservation success audit
        verify(auditService).audit(
                "ORD-1001",
                "INVENTORY",
                "SUCCESS",
                "CORR-001"
        );

        // Verify inventory compensation audit
        verify(auditService).audit(
                "ORD-1001",
                "INVENTORY_RELEASE",
                "SUCCESS",
                "CORR-001"
        );

        // Verify payment failure audit
        verify(auditService).audit(
                "ORD-1001",
                "PAYMENT",
                "FAILED",
                "CORR-001"
        );
    }

    @Test
    void shouldPublishShipmentWhenPaymentSucceeds() {

        // Arrange
        when(inventoryClient.reserve(any()))
                .thenReturn(
                        new InventoryResponse(
                                true,
                                null
                        )
                );

        when(paymentClient.authorise(any()))
                .thenReturn(
                        new PaymentResponse(
                                true,
                                "TXN-123"
                        )
                );

        // Act
        orderOrchestrationService.process(order, request);

        // Assert: order status
        assertEquals(
                OrderStatus.SHIPMENT_REQUESTED,
                order.getStatus()
        );

        // Verify inventory reservation
        verify(inventoryClient, times(1))
                .reserve(any());

        // Verify payment authorization
        verify(paymentClient, times(1))
                .authorise(any());

        // Inventory should not be released
        verify(inventoryClient, never())
                .release(any());

        // Verify order was saved after reservation and payment authorization
        verify(orderRepository, times(3))
                .save(order);

        // Capture and verify the published shipment event
        ArgumentCaptor<ShipmentRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(ShipmentRequestedEvent.class);

        verify(shipmentPublisher, times(1))
                .publish(eventCaptor.capture());

        ShipmentRequestedEvent publishedEvent =
                eventCaptor.getValue();

        assertEquals(
                "ORD-1001",
                publishedEvent.orderId()
        );

        assertEquals(
                "CUST-1001",
                publishedEvent.customerId()
        );

        assertEquals(
                "CORR-001",
                publishedEvent.correlationId()
        );

        // Verify inventory reservation success audit
        verify(auditService).audit(
                "ORD-1001",
                "INVENTORY",
                "SUCCESS",
                "CORR-001"
        );

        // Verify payment success audit
        verify(auditService).audit(
                "ORD-1001",
                "PAYMENT",
                "SUCCESS",
                "CORR-001"
        );

        // Verify shipment request audit
        verify(auditService).audit(
                "ORD-1001",
                "SHIPMENT",
                "REQUESTED",
                "CORR-001"
        );
    }
}
