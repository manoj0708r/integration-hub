
package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.domain.entity.IdempotencyRecord;
import com.aspire.poc.integration_hub.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository repository;

    public Optional<String> findByKey(String key) {

        Optional<String> orderId = repository.findById(key)
                .map(IdempotencyRecord::getOrderId);

        if (orderId.isPresent()) {
            log.info(
                    "Existing idempotency record found. orderId={}",
                    orderId.get()
            );
        } else {
            log.debug("No existing idempotency record found.");
        }

        return orderId;
    }

    public void save(String key, String orderId) {

        IdempotencyRecord record = new IdempotencyRecord();

        record.setIdempotencyKey(key);
        record.setOrderId(orderId);
        record.setCreatedAt(LocalDateTime.now());

        repository.save(record);

        log.info(
                "Idempotency record saved successfully. orderId={}",
                orderId
        );
    }
}
