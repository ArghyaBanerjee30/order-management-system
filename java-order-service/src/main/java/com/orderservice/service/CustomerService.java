package com.orderservice.service;

import com.orderservice.dto.CreateCustomerRequest;
import com.orderservice.dto.CustomerResponse;
import com.orderservice.dto.UpdateCustomerRequest;
import com.orderservice.entity.Customer;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.DuplicateCustomerException;
import com.orderservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Customer business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Create a new customer.
     *
     * @param request customer creation request
     * @return created customer response
     * @throws RuntimeException if email already exists
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.error("Email already exists: {}", request.getEmail());
            throw new DuplicateCustomerException(request.getEmail(), "create customer");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return mapToResponse(savedCustomer);
    }

    /**
     * Get customer by ID.
     *
     * @param id customer ID
     * @return customer response
     * @throws RuntimeException if customer not found
     */
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", id);
                    return new CustomerNotFoundException(id);
                });
        log.debug("Customer found: {}", customer.getEmail());
        return mapToResponse(customer);
    }

    /**
     * Get all customers.
     *
     * @return list of customer responses
     */
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        List<CustomerResponse> customers = customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} customers", customers.size());
        return customers;
    }

    /**
     * Update an existing customer.
     *
     * @param id customer ID
     * @param request update request
     * @return updated customer response
     * @throws RuntimeException if customer not found or email already exists
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", id);
                    return new CustomerNotFoundException(id);
                });

        // Check if email is being updated and if it already exists
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                log.error("Email already exists: {}", request.getEmail());
                throw new DuplicateCustomerException(request.getEmail(), "update customer");
            }
            customer.setEmail(request.getEmail());
        }

        // Update fields if provided
        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully: {}", id);
        return mapToResponse(updatedCustomer);
    }

    /**
     * Delete a customer.
     *
     * @param id customer ID
     * @throws RuntimeException if customer not found
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            log.error("Customer not found with ID: {}", id);
            throw new CustomerNotFoundException(id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully: {}", id);
    }

    /**
     * Map Customer entity to CustomerResponse DTO.
     *
     * @param customer customer entity
     * @return customer response DTO
     */
    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }
}
