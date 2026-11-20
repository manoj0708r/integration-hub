package com.aspire.poc.integration_hub.messaging.listener;

import com.aspire.poc.integration_hub.api.exception.OrderNotFoundException;
import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import com.aspire.poc.integration_hub.messaging.dto.ShipmentStatusEvent;
import com.aspire.poc.integration_hub.repository.OrderRepository;
import com.aspire.poc.integration_hub.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentStatusListener {

    private final OrderRepository orderRepository;
    private final AuditService auditService;

    @JmsListener(destination = "warehouse.status")
    @Transactional
    public void handleShipmentStatus(ShipmentStatusEvent event) {

        String orderId = event.orderId();

        log.info("Received shipment status event. orderId={}, status={}",
                orderId, event.status());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate the received shipment status.
        if (!OrderStatus.SHIPPED.name().equalsIgnoreCase(event.status())) {

            log.warn("Unsupported shipment status received. orderId={}, status={}",
                    orderId, event.status());

            return;
        }

        // Prevent duplicate shipment status processing.
        if (order.getStatus() == OrderStatus.SHIPPED) {

            log.info("Shipment already processed. Skipping duplicate event. orderId={}",
                    orderId);

            return;
        }

        // Process only orders awaiting shipment confirmation.
        if (order.getStatus() != OrderStatus.SHIPMENT_REQUESTED) {

            log.warn("Shipment confirmation received for an unexpected order status. "
                            + "orderId={}, currentStatus={}",
                    orderId, order.getStatus());

            return;
        }

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        auditService.audit(
                orderId,
                "SHIPMENT",
                "SHIPPED",
                order.getCorrelationId()
        );

        log.info("Order shipment completed successfully. orderId={}, correlationId={}",
                orderId, order.getCorrelationId());
    }
}
