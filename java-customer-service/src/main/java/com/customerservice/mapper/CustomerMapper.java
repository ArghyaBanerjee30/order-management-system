package com.customerservice.mapper;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.entity.Customer;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class CustomerMapper {

    private CustomerMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Customer toEntity(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return customer;
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt()
        );
    }

    public static void applyUpdate(Customer customer, UpdateCustomerRequest request) {
        updateFieldIfPresent(request.getFirstName(), customer::setFirstName);
        updateFieldIfPresent(request.getLastName(), customer::setLastName);
        updateFieldIfPresent(request.getPhone(), customer::setPhone);
    }

    public static void updateEmailIfDifferent(Customer customer, String newEmail, BiConsumer<Customer, String> emailSetter) {
        Optional.ofNullable(newEmail)
                .filter(email -> !email.equals(customer.getEmail()))
                .ifPresent(email -> emailSetter.accept(customer, email));
    }

    private static <T> void updateFieldIfPresent(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }
}
