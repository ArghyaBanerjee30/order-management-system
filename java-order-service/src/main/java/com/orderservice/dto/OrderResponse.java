package com.orderservice.dto;

import com.orderservice.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for order response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order information response")
public class OrderResponse {

    @Schema(description = "Order's unique identifier", example = "1")
    private Long id;

    @Schema(description = "Customer ID who placed the order", example = "1")
    private Long customerId;

    @Schema(description = "Current order status", example = "CONFIRMED")
    private OrderStatus status;

    @Schema(description = "Total order amount", example = "499.95")
    private BigDecimal totalAmount;

    @Schema(description = "Timestamp when order was created", example = "2026-07-21T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when order was last updated", example = "2026-07-21T10:31:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of items in the order")
    private List<OrderItemDTO> orderItems = new ArrayList<>();
}
