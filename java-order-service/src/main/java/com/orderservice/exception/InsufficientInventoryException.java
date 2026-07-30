package com.orderservice.exception;

/**
 * Exception thrown when there is insufficient inventory to fulfill an order.
 */
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String message) {
        super(message);
    }

    public InsufficientInventoryException(Long productId, Integer requested, Integer available) {
        super("Insufficient inventory for product " + productId +
                ". Requested: " + requested + ", Available: " + available);
    }
}
