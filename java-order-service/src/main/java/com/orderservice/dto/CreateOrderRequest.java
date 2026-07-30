package com.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a new order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new order")
public class CreateOrderRequest {

    @Schema(description = "Customer ID who is placing the order", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @Schema(description = "List of items in the order", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> orderItems;
}
