package com.aspire.poc.integration_hub.messaging.listener;

import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.messaging.dto.ShipmentStatusEvent;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentStatusListener {

    private final OrderRepository orderRepository;

    private final AuditService auditService;

    @JmsListener(destination = "warehouse.status")
    public void handleShipmentStatus(ShipmentStatusEvent event) {

        Order order = orderRepository.findById(event.orderId()).orElseThrow();

        order.setStatus(OrderStatus.SHIPPED);

        orderRepository.save(order);

        auditService.audit(order.getOrderId(), "SHIPMENT", "SHIPPED", "ARTEMIS");
    }
}