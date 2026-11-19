package com.aspire.poc.integration_hub.repository;

import com.aspire.poc.integration_hub.domain.entity.Order;
import com.aspire.poc.integration_hub.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Modifying
    @Query("""
            UPDATE Order o
            SET o.status = :status,
                o.updatedAt = CURRENT_TIMESTAMP
            WHERE o.orderId = :orderId
            """)
    void updateStatus(String orderId, OrderStatus status);
}
