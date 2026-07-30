package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for inventory release response from inventory service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReleaseResponse {

    private boolean success;
    private String message;
    private InventoryDTO inventory;
}
