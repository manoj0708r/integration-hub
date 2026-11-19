package com.aspire.poc.integration_hub.repository;

import com.aspire.poc.integration_hub.domain.entity.OrderAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAuditRepository
        extends JpaRepository<OrderAudit, Long> {

    List<OrderAudit> findByOrderIdOrderByCreatedAtAsc(
            String orderId
    );
}