package com.customerservice.dto;

import com.customerservice.constants.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @Size(min = 1, max = 100, message = ValidationMessages.FIRST_NAME_SIZE_RANGE)
    private String firstName;

    @Size(min = 1, max = 100, message = ValidationMessages.LAST_NAME_SIZE_RANGE)
    private String lastName;

    @Email(message = ValidationMessages.EMAIL_INVALID)
    @Size(max = 255, message = ValidationMessages.EMAIL_MAX_LENGTH)
    private String email;

    @Pattern(regexp = ValidationMessages.PHONE_PATTERN, message = ValidationMessages.PHONE_INVALID_FORMAT)
    private String phone;
}
