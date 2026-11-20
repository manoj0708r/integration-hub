
package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.api.dto.AuditResponse;
import com.aspire.poc.integration_hub.api.dto.CreateOrderRequest;
import com.aspire.poc.integration_hub.api.dto.CreateOrderResponse;
import com.aspire.poc.integration_hub.api.dto.OrderResponse;
import com.aspire.poc.integration_hub.api.exception.OrderNotFoundException;
import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.entity.OrderItem;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.repository.OrderAuditRepository;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.impl.OrderOrchestrationService;
import com.aspire.poc.integration_hub.util.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderAuditRepository auditRepository;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;
    private final OrderIdGenerator orderIdGenerator;
    private final OrderOrchestrationService orderOrchestrationService;

    @Transactional
    public CreateOrderResponse createOrder(
            CreateOrderRequest request,
            String idempotencyKey,
            String correlationId) {

        log.info(
                "Received order creation request. customerId={}, correlationId={}",
                request.customerId(),
                correlationId
        );

        // Check existing idempotency record
        var existingOrderId = idempotencyService.findByKey(idempotencyKey);

        if (existingOrderId.isPresent()) {

            String existingId = existingOrderId.get();

            Order existingOrder = orderRepository.findById(existingId)
                    .orElseThrow(() -> new OrderNotFoundException(existingId));

            log.info(
                    "Duplicate request detected. Returning existing order. orderId={}",
                    existingId
            );

            return new CreateOrderResponse(
                    existingOrder.getOrderId(),
                    existingOrder.getStatus().name(),
                    existingOrder.getCorrelationId()
            );
        }

        // Generate order ID
        String orderId = orderIdGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();

        // Create order
        Order order = Order.builder()
                .orderId(orderId)
                .customerId(request.customerId())
                .totalAmount(request.totalAmount())
                .currency(request.currency())
                .correlationId(correlationId)
                .status(OrderStatus.RECEIVED)
                .exported(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Add order items
        request.items().forEach(itemRequest -> {

            OrderItem orderItem = OrderItem.builder()
                    .sku(itemRequest.sku())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
        });

        // Save order
        orderRepository.save(order);

        log.info("Order created successfully. orderId={}", orderId);

        // Save idempotency record
        idempotencyService.save(idempotencyKey, orderId);

        // Record order creation audit
        auditService.audit(
                orderId,
                "ORDER_CREATED",
                "SUCCESS",
                correlationId
        );

        // Start orchestration
        orderOrchestrationService.process(order, request);

        // Retrieve latest persisted order
        Order processedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        log.info(
                "Order processing completed. orderId={}, status={}, correlationId={}",
                processedOrder.getOrderId(),
                processedOrder.getStatus(),
                processedOrder.getCorrelationId()
        );

        return new CreateOrderResponse(
                processedOrder.getOrderId(),
                processedOrder.getStatus().name(),
                processedOrder.getCorrelationId()
        );
    }


    public OrderResponse getOrder(String orderId) {

        log.info("Fetching order details. orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found. orderId={}", orderId);
                    return new OrderNotFoundException(orderId);
                });

        List<AuditResponse> audits = auditRepository
                .findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(audit -> new AuditResponse(
                        audit.getStep(),
                        audit.getOutcome(),
                        audit.getCreatedAt()
                ))
                .toList();

        log.info(
                "Order details retrieved successfully. orderId={}",
                orderId
        );

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getStatus().name(),
                audits
        );
    }
}
