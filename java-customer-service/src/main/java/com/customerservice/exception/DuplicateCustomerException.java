package com.customerservice.exception;

import com.customerservice.constants.ErrorMessages;

public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String email, String operation) {
        super(String.format(ErrorMessages.CUSTOMER_DUPLICATE_EMAIL, email, operation));
    }
}
