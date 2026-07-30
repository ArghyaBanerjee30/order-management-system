package com.customerservice.constants;

public final class ValidationMessages {

    private ValidationMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String FIRST_NAME_MAX_LENGTH = "First name must not exceed 100 characters";
    public static final String FIRST_NAME_SIZE_RANGE = "First name must be between 1 and 100 characters";

    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String LAST_NAME_MAX_LENGTH = "Last name must not exceed 100 characters";
    public static final String LAST_NAME_SIZE_RANGE = "Last name must be between 1 and 100 characters";

    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email must be valid";
    public static final String EMAIL_MAX_LENGTH = "Email must not exceed 255 characters";

    public static final String PHONE_INVALID_FORMAT = "Phone number must be in valid format";
    public static final String PHONE_PATTERN = "^[0-9+\\-()\\ ]*$";
}
