package com.orderservice.exception;

/**
 * Exception thrown when order validation fails.
 */
public class OrderValidationException extends RuntimeException {

    public OrderValidationException(String message) {
        super(message);
    }

    public OrderValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
