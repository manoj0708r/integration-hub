
package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.domain.entity.OrderAudit;
import com.aspire.poc.integration_hub.repository.OrderAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final OrderAuditRepository repository;

    public void audit(
            String orderId,
            String step,
            String outcome,
            String correlationId) {

        OrderAudit audit = OrderAudit.builder()
                .orderId(orderId)
                .step(step)
                .outcome(outcome)
                .correlationId(correlationId)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(audit);

        log.info(
                "Order audit recorded. orderId={}, step={}, outcome={}, correlationId={}",
                orderId,
                step,
                outcome,
                correlationId
        );
    }
}
