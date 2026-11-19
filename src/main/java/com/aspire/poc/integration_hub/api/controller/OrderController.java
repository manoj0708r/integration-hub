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
        name = "Order Management",
        description = """
                Order Fulfilment Integration Hub APIs.

                Supports:
                - Order Creation
                - Order Retrieval
                - Idempotency Handling
                - Inventory Reservation
                - Payment Authorisation
                - Compensation Flow
                - Audit Trail Tracking
                """
)
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create Order",
            description = """
                    Creates a new customer order.

                    Processing Flow:

                    RECEIVED
                        ↓
                    INVENTORY_RESERVE
                        ↓
                    PAYMENT_AUTHORISE
                        ↓
                    PAYMENT_AUTHORISED

                    Failure Flows:

                    Inventory Failure
                        ↓
                    REJECTED

                    Payment Failure
                        ↓
                    INVENTORY_RELEASE
                        ↓
                    PAYMENT_FAILED

                    Duplicate requests are handled through the
                    Idempotency-Key header.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Order processed successfully",
                    content = @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            CreateOrderResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failure"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid API key"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description =
                            "Inventory or Payment service unavailable"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(

            @Parameter(
                    name = "X-API-Key",
                    description =
                            "API Key configured in application.yml",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-dev-key"
            )
            @RequestHeader("X-API-Key")
            String apiKey,

            @Parameter(
                    name = "X-Correlation-Id",
                    description =
                            "Optional correlation identifier used for distributed tracing",
                    required = false,
                    in = ParameterIn.HEADER,
                    example = "CORR-123456"
            )
            @RequestHeader(
                    value = "X-Correlation-Id",
                    required = false
            )
            String correlationId,

            @Parameter(
                    name = "Idempotency-Key",
                    description =
                            "Unique key used to prevent duplicate order submissions",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "ORDER-REQ-001"
            )
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateOrderRequest request
    ) {

        return ResponseEntity.accepted()
                .body(
                        orderService.createOrder(
                                request,
                                idempotencyKey, correlationId
                        )
                );
    }


    @Operation(
            summary = "Get Order Details",
            description = """
                    Retrieve an order and complete audit trail.

                    Audit Trail Contains:

                    - ORDER_CREATED
                    - INVENTORY
                    - INVENTORY_RELEASE
                    - PAYMENT

                    along with timestamps and outcomes.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            OrderResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid API key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found"
            )
    })
    @GetMapping("/{id}")
    public OrderResponse getOrder(

            @Parameter(
                    name = "X-API-Key",
                    description =
                            "API Key configured in application.yml",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "local-dev-key"
            )
            @RequestHeader("X-API-Key")
            String apiKey,

            @Parameter(
                    description = "Unique Order Identifier",
                    required = true,
                    example = "ORD-B683A0F0"
            )
            @PathVariable
            String id
    ) {

        return orderService.getOrder(id);
    }
}