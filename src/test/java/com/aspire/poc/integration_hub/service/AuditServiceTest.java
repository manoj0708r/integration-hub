package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.domain.entity.OrderAudit;
import com.aspire.poc.integration_hub.repository.OrderAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private OrderAuditRepository repository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldCreateAuditRecord() {

        auditService.audit(
                "ORD-1001",
                "PAYMENT",
                "SUCCESS",
                "CORR-001"
        );

        ArgumentCaptor<OrderAudit> captor =
                ArgumentCaptor.forClass(OrderAudit.class);

        verify(repository).save(captor.capture());

        OrderAudit saved = captor.getValue();

        assertEquals("ORD-1001", saved.getOrderId());
        assertEquals("PAYMENT", saved.getStep());
        assertEquals("SUCCESS", saved.getOutcome());
        assertEquals("CORR-001", saved.getCorrelationId());
        assertNotNull(saved.getCreatedAt());
    }
}