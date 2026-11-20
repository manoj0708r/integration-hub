package com.aspire.poc.integration_hub.messaging.listener;

import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import com.aspire.poc.integration_hub.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeadLetterListenerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeadLetterListener deadLetterListener;

    @Test
    void shouldAuditDeadLetterEvent() {

        ShipmentRequestedEvent event =
                new ShipmentRequestedEvent(
                        "ORD-1001",
                        "CUST-1001",
                        "CORR-001"
                );

        deadLetterListener.processDeadLetter(event);

        verify(auditService)
                .audit(
                        "ORD-1001",
                        "DLQ",
                        "RECEIVED",
                        "CORR-001"
                );
    }
}