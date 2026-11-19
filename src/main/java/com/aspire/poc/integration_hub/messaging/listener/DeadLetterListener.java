package com.aspire.poc.integration_hub.messaging.listener;

import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import com.aspire.poc.integration_hub.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeadLetterListener {

    private final AuditService auditService;

    @JmsListener(destination = "warehouse.dlq")
    public void processDeadLetter(ShipmentRequestedEvent event) {

        auditService.audit(event.orderId(), "DLQ", "RECEIVED", "ARTEMIS");

        System.out.println("DLQ Message Received : " + event.orderId());
    }
}