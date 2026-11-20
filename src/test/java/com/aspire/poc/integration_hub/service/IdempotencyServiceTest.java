package com.aspire.poc.integration_hub.service;

import com.aspire.poc.integration_hub.domain.entity.IdempotencyRecord;
import com.aspire.poc.integration_hub.repository.IdempotencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyRepository repository;

    @InjectMocks
    private IdempotencyService service;

    @Test
    void shouldReturnOrderIdWhenRecordExists() {

        IdempotencyRecord record = new IdempotencyRecord();

        record.setIdempotencyKey("KEY-1");
        record.setOrderId("ORD-1001");

        when(repository.findById("KEY-1"))
                .thenReturn(Optional.of(record));

        Optional<String> result =
                service.findByKey("KEY-1");

        assertTrue(result.isPresent());
        assertEquals("ORD-1001", result.get());
    }

    @Test
    void shouldReturnEmptyWhenRecordDoesNotExist() {

        when(repository.findById("KEY-1"))
                .thenReturn(Optional.empty());

        Optional<String> result =
                service.findByKey("KEY-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveIdempotencyRecord() {

        service.save(
                "KEY-1",
                "ORD-1001"
        );

        ArgumentCaptor<IdempotencyRecord> captor =
                ArgumentCaptor.forClass(IdempotencyRecord.class);

        verify(repository)
                .save(captor.capture());

        IdempotencyRecord saved =
                captor.getValue();

        assertEquals(
                "KEY-1",
                saved.getIdempotencyKey()
        );

        assertEquals(
                "ORD-1001",
                saved.getOrderId()
        );

        assertNotNull(saved.getCreatedAt());
    }
}