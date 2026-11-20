package com.aspire.poc.integration_hub.api.controller;

import com.aspire.poc.integration_hub.api.dto.CreateOrderRequest;
import com.aspire.poc.integration_hub.api.dto.CreateOrderResponse;
import com.aspire.poc.integration_hub.api.dto.OrderItemRequest;
import com.aspire.poc.integration_hub.api.dto.OrderResponse;
import com.aspire.poc.integration_hub.config.ApiKeyFilter;
import com.aspire.poc.integration_hub.config.ApplicationProperties;
import com.aspire.poc.integration_hub.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ApiKeyFilter apiKeyFilter;

    @MockitoBean
    private ApplicationProperties applicationProperties;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        "CUST-1001",
                        List.of(
                                new OrderItemRequest(
                                        "SKU-IPHONE-16",
                                        1,
                                        BigDecimal.valueOf(85000)
                                )
                        ),
                        BigDecimal.valueOf(85000),
                        "INR"
                );

        when(orderService.createOrder(
                any(),
                anyString(),
                anyString()))
                .thenReturn(
                        new CreateOrderResponse(
                                "ORD-1001",
                                "RECEIVED",
                                "CORR-001"
                        )
                );

        mockMvc.perform(
                        post("/api/orders")
                                .requestAttr("correlationId", "CORR-001")
                                .header("X-API-Key", "Aspire-POC")
                                .header("Idempotency-Key", "TEST-001")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {

        String invalidRequest = """
                {
                  "customerId": "",
                  "items": [],
                  "totalAmount": -1,
                  "currency": ""
                }
                """;

        mockMvc.perform(
                        post("/api/orders")
                                .requestAttr("correlationId", "CORR-001")
                                .header("X-API-Key", "Aspire-POC")
                                .header("Idempotency-Key", "TEST-001")
                                .contentType(APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetOrderSuccessfully() throws Exception {

        OrderResponse response =
                new OrderResponse(
                        "ORD-1001",
                        "CUST-1001",
                        "SHIPPED",
                        List.of()
                );

        when(orderService.getOrder("ORD-1001"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/orders/ORD-1001")
                                .header("X-API-Key", "Aspire-POC")
                )
                .andExpect(status().isOk());
    }
}