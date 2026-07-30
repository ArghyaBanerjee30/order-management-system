package com.orderservice.exception;

import com.orderservice.constants.ErrorMessages;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Long id) {
        super(String.format(ErrorMessages.CUSTOMER_NOT_FOUND, id));
    }
}
