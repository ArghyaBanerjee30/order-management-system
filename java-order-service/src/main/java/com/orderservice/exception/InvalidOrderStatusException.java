package com.orderservice.exception;

import com.orderservice.entity.OrderStatus;

/**
 * Exception thrown when an order operation is attempted with an invalid status.
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(Long orderId, OrderStatus currentStatus, String operation) {
        super("Cannot " + operation + " order " + orderId + " with status " + currentStatus);
    }
}
