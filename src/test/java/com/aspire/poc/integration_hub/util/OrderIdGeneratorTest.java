package com.aspire.poc.integration_hub.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderIdGeneratorTest {

    private final OrderIdGenerator generator = new OrderIdGenerator();

    @Test
    void shouldGenerateOrderId() {

        String id = generator.nextId();

        assertNotNull(id);

        assertTrue(id.startsWith("ORD-"));

        assertEquals(16, id.length());

        assertTrue(
                id.matches("ORD-[A-Z0-9]{12}")
        );
    }

    @Test
    void shouldGenerateUniqueIds() {

        String first = generator.nextId();

        String second = generator.nextId();

        assertNotEquals(first, second);
    }
}