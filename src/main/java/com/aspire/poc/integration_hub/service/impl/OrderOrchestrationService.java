package com.aspire.poc.integration_hub.service.impl;

import com.aspire.poc.integration_hub.api.dto.CreateOrderRequest;
import com.aspire.poc.integration_hub.client.InventoryClient;
import com.aspire.poc.integration_hub.client.PaymentClient;
import com.aspire.poc.integration_hub.client.dto.InventoryRequest;
import com.aspire.poc.integration_hub.client.dto.InventoryResponse;
import com.aspire.poc.integration_hub.client.dto.PaymentRequest;
import com.aspire.poc.integration_hub.client.dto.PaymentResponse;
import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import com.aspire.poc.integration_hub.messaging.publisher.ShipmentPublisher;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final AuditService auditService;
    private final ShipmentPublisher shipmentPublisher;

    public void process(Order order, CreateOrderRequest request) {

        String orderId = order.getOrderId();
        String correlationId = order.getCorrelationId();

        log.info(
                "Starting order orchestration. orderId={}, correlationId={}",
                orderId, correlationId
        );

        // Step 1: Reserve inventory
        log.info("Requesting inventory reservation. orderId={}", orderId);

        InventoryResponse inventoryResponse =
                inventoryClient.reserve(
                        new InventoryRequest(orderId, request.items())
                );

        if (!inventoryResponse.reserved()) {

            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);

            auditService.audit(
                    orderId,
                    "INVENTORY",
                    "REJECTED",
                    correlationId
            );

            log.warn(
                    "Order rejected because inventory reservation failed. orderId={}",
                    orderId
            );

            return;
        }

        order.setStatus(OrderStatus.STOCK_RESERVED);
        orderRepository.save(order);

        auditService.audit(
                orderId,
                "INVENTORY",
                "SUCCESS",
                correlationId
        );

        log.info("Inventory successfully reserved. orderId={}", orderId);

        // Step 2: Authorize payment
        log.info("Requesting payment authorization. orderId={}", orderId);

        PaymentResponse paymentResponse =
                paymentClient.authorise(
                        new PaymentRequest(
                                orderId,
                                request.customerId(),
                                request.totalAmount()
                        )
                );

        if (!paymentResponse.authorised()) {

            log.warn(
                    "Payment authorization failed. Starting inventory compensation. orderId={}",
                    orderId
            );

            // Compensation: Release reserved inventory
            inventoryClient.release(
                    new InventoryRequest(orderId, request.items())
            );

            auditService.audit(
                    orderId,
                    "INVENTORY_RELEASE",
                    "SUCCESS",
                    correlationId
            );

            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            auditService.audit(
                    orderId,
                    "PAYMENT",
                    "FAILED",
                    correlationId
            );

            log.warn(
                    "Payment failed and inventory compensation completed. orderId={}",
                    orderId
            );

            return;
        }

        order.setStatus(OrderStatus.PAYMENT_AUTHORISED);
        orderRepository.save(order);

        auditService.audit(
                orderId,
                "PAYMENT",
                "SUCCESS",
                correlationId
        );

        log.info("Payment successfully authorized. orderId={}", orderId);

        // Step 3: Publish shipment request
        ShipmentRequestedEvent event =
                new ShipmentRequestedEvent(
                        orderId,
                        order.getCustomerId(),
                        correlationId
                );

        shipmentPublisher.publish(event);

        order.setStatus(OrderStatus.SHIPMENT_REQUESTED);
        orderRepository.save(order);

        auditService.audit(
                orderId,
                "SHIPMENT",
                "REQUESTED",
                correlationId
        );

        log.info(
                "Shipment request published successfully. orderId={}, correlationId={}",
                orderId, correlationId
        );
    }
}
