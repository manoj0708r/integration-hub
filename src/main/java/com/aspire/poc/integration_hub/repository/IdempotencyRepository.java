package com.aspire.poc.integration_hub.repository;


import com.aspire.poc.integration_hub.domain.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository
        extends JpaRepository<IdempotencyRecord,String> {
}
