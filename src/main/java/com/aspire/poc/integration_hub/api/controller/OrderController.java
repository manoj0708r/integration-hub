package com.aspire.poc.integration_hub.api.controller;

import com.aspire.poc.integration_hub.api.dto.CreateOrderRequest;
import com.aspire.poc.integration_hub.api.dto.CreateOrderResponse;
import com.aspire.poc.integration_hub.api.dto.OrderResponse;
import com.aspire.poc.integration_hub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(
        name = "Orders",
        description = "APIs for creating and retrieving customer orders."
)
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create an order",
            description = "Creates an order and initiates inventory reservation and payment authorization."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Order accepted successfully",
                    content = @Content(
                            schema = @Schema(implementation = CreateOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Inventory reservation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Payment authorization failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "504",
                    description = "Payment service timeout",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(

            @Parameter(
                    name = "X-API-Key",
                    description = "API authentication key",
                    in = ParameterIn.HEADER,
                    required = true,
                    example = "local-dev-key"
            )
            @RequestHeader("X-API-Key")
            String apiKey,

            @Parameter(hidden = true)
            @RequestAttribute("correlationId")
            String correlationId,

            @Parameter(
                    name = "Idempotency-Key",
                    description = "Unique key to prevent duplicate order creation",
                    in = ParameterIn.HEADER,
                    required = true,
                    example = "ORDER-REQ-001"
            )
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderService.createOrder(
                request,
                idempotencyKey,
                correlationId
        );

        return ResponseEntity.accepted().body(response);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves order details using the order identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public OrderResponse getOrder(

            @Parameter(
                    name = "X-API-Key",
                    description = "API authentication key",
                    in = ParameterIn.HEADER,
                    required = true,
                    example = "local-dev-key"
            )
            @RequestHeader("X-API-Key")
            String apiKey,

            @Parameter(
                    description = "Order identifier",
                    required = true,
                    example = "ORD-B683A0F0"
            )
            @PathVariable("id")
            String id
    ) {
        return orderService.getOrder(id);
    }
}

