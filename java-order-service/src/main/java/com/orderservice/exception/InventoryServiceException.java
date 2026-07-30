package com.orderservice.exception;

/**
 * Exception thrown when there is an error communicating with the inventory service.
 */
public class InventoryServiceException extends RuntimeException {

    public InventoryServiceException(String message) {
        super(message);
    }

    public InventoryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
