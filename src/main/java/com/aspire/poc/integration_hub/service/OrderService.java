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
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
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

        var existing = idempotencyService.findByKey(idempotencyKey);

        if (existing.isPresent()) {

            Order order = orderRepository.findById(existing.get()).orElseThrow(() -> new OrderNotFoundException(existing.get()));

            return new CreateOrderResponse(order.getOrderId(), order.getStatus().name(),  order.getCorrelationId());
        }

        String orderId = orderIdGenerator.nextId();

        LocalDateTime now = LocalDateTime.now();

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

        request.items().forEach(itemRequest -> {

            OrderItem orderItem = OrderItem.builder()
                    .sku(itemRequest.sku())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
        });

        Order savedOrder = orderRepository.save(order);

        idempotencyService.save(idempotencyKey, orderId);

        auditService.audit(orderId, "ORDER_CREATED", "SUCCESS", "LOCAL");

        orderOrchestrationService.process(savedOrder, request);

        Order processedOrder = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        return new CreateOrderResponse(processedOrder.getOrderId(), processedOrder.getStatus().name(), processedOrder.getCorrelationId());
    }

    public OrderResponse getOrder(String orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        List<AuditResponse> audits = auditRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(x -> new AuditResponse(x.getStep(), x.getOutcome(), x.getCreatedAt())).toList();

        return new OrderResponse(order.getOrderId(), order.getCustomerId(), order.getStatus().name(), audits);
    }
}