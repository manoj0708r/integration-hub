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
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.AuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderOrchestrationService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    private final PaymentClient paymentClient;

    private final AuditService auditService;

    public void process(Order order, CreateOrderRequest request) {

        InventoryResponse inventoryResponse = inventoryClient.reserve(new InventoryRequest(order.getOrderId(), request.items()));

        if (!inventoryResponse.reserved()) {

            order.setStatus(OrderStatus.REJECTED);

            orderRepository.save(order);

            auditService.audit(order.getOrderId(), "INVENTORY", "REJECTED", "LOCAL");

            return;
        }

        order.setStatus(OrderStatus.STOCK_RESERVED);

        orderRepository.save(order);

        auditService.audit(order.getOrderId(), "INVENTORY", "SUCCESS", "LOCAL");

        PaymentResponse paymentResponse = paymentClient.authorise(new PaymentRequest(order.getOrderId(), request.customerId(), request.totalAmount()));

        if (!paymentResponse.authorised()) {

            inventoryClient.release(new InventoryRequest(order.getOrderId(), request.items()));

            auditService.audit(order.getOrderId(), "INVENTORY_RELEASE", "SUCCESS", "LOCAL");

            order.setStatus(OrderStatus.PAYMENT_FAILED);

            orderRepository.save(order);

            auditService.audit(order.getOrderId(), "PAYMENT", "FAILED", "LOCAL");

            return;
        }

        order.setStatus(OrderStatus.PAYMENT_AUTHORISED);

        orderRepository.save(order);

        auditService.audit(order.getOrderId(), "PAYMENT", "SUCCESS", "LOCAL");
    }


}
