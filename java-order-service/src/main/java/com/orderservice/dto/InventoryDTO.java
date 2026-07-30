package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for inventory information from inventory service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {

    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private LocalDateTime lastUpdated;
}
