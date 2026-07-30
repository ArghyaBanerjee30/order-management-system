package com.customerservice.service;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.entity.Customer;
import com.customerservice.exception.CustomerNotFoundException;
import com.customerservice.exception.DuplicateCustomerException;
import com.customerservice.mapper.CustomerMapper;
import com.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final String CREATE_OPERATION = "create";
    private static final String UPDATE_OPERATION = "update";

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        validateEmailNotExists(request.getEmail(), CREATE_OPERATION);
        return saveAndConvert(CustomerMapper.toEntity(request));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return findCustomerById(id)
                .map(CustomerMapper::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        return findCustomerById(id)
                .map(customer -> applyUpdates(customer, request))
                .map(this::saveAndConvert)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        findCustomerById(id)
                .ifPresentOrElse(
                        customerRepository::delete,
                        throwCustomerNotFound(id)
                );
    }

    private Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    private void validateEmailNotExists(String email, String operation) {
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateCustomerException(email, operation);
        }
    }

    private Customer applyUpdates(Customer customer, UpdateCustomerRequest request) {
        Optional.ofNullable(request.getEmail())
                .filter(email -> !email.equals(customer.getEmail()))
                .ifPresent(email -> {
                    validateEmailNotExists(email, UPDATE_OPERATION);
                    customer.setEmail(email);
                });

        CustomerMapper.applyUpdate(customer, request);
        return customer;
    }

    private CustomerResponse saveAndConvert(Customer customer) {
        return Optional.of(customer)
                .map(customerRepository::save)
                .map(CustomerMapper::toResponse)
                .orElseThrow();
    }

    private Runnable throwCustomerNotFound(Long id) {
        return () -> {
            throw new CustomerNotFoundException(id);
        };
    }
}
