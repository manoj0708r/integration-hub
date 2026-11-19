package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.domain.entity.OrderAudit;
import com.aspire.poc.integration_hub.repository.OrderAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final OrderAuditRepository repository;

    public void audit(String orderId, String step, String outcome, String correlationId) {

        OrderAudit audit = OrderAudit.builder().orderId(orderId)
                .step(step)
                .outcome(outcome)
                .correlationId(correlationId)
                .createdAt(LocalDateTime.now()).build();

        repository.save(audit);
    }
}
