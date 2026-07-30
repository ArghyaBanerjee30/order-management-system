package com.orderservice.exception;

import com.orderservice.constants.ErrorMessages;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long id) {
        super(String.format(ErrorMessages.ORDER_NOT_FOUND, id));
    }
}
