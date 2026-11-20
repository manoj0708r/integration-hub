package com.aspire.poc.integration_hub.messaging.listener;

import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import com.aspire.poc.integration_hub.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterListener {

    private final AuditService auditService;

    @JmsListener(destination = "warehouse.dlq")
    @Transactional
    public void processDeadLetter(ShipmentRequestedEvent event) {

        log.warn("Shipment request moved to dead-letter queue. "
                        + "orderId={}, correlationId={}",
                event.orderId(), event.correlationId());

        auditService.audit(
                event.orderId(),
                "DLQ",
                "RECEIVED",
                event.correlationId()
        );

        log.info("Dead-letter event audited successfully. orderId={}",
                event.orderId());
    }
}
