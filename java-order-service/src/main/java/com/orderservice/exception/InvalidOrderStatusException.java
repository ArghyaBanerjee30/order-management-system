package com.orderservice.exception;

import com.orderservice.constants.ErrorMessages;
import com.orderservice.entity.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(Long orderId, OrderStatus currentStatus, String operation) {
        super(String.format(ErrorMessages.INVALID_ORDER_STATUS, operation, orderId, currentStatus));
    }
}
