package com.orderservice.exception;

/**
 * Exception thrown when inventory reservation fails.
 */
public class InventoryReservationException extends RuntimeException {

    public InventoryReservationException(String message) {
        super(message);
    }

    public InventoryReservationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryReservationException(Long productId, Integer quantity) {
        super("Failed to reserve " + quantity + " units of product " + productId);
    }
}
