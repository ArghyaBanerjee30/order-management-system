package com.customerservice.exception;

import com.customerservice.constants.ErrorMessages;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super(String.format(ErrorMessages.CUSTOMER_NOT_FOUND, id));
    }
}
