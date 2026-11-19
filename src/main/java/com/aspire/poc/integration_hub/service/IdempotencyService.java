package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.domain.entity.IdempotencyRecord;
import com.aspire.poc.integration_hub.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository repository;

    public Optional<String> findByKey(String key) {
        return repository.findById(key).map(IdempotencyRecord::getOrderId);
    }

    public void save(String key, String orderId) {
        IdempotencyRecord record = new IdempotencyRecord();

        record.setIdempotencyKey(key);
        record.setOrderId(orderId);
        record.setCreatedAt(LocalDateTime.now());
        repository.save(record);
    }
}
