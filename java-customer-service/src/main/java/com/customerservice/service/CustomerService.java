package com.customerservice.service;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.entity.Customer;
import com.customerservice.exception.CustomerNotFoundException;
import com.customerservice.exception.DuplicateCustomerException;
import com.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

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
        log.info("Customer created with id: {}", savedCustomer.getId());

        return CustomerResponse.fromEntity(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return CustomerResponse.fromEntity(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");

        return customerRepository.findAll()
                .stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                log.error("Email already exists: {}", request.getEmail());
                throw new DuplicateCustomerException(request.getEmail(), "update customer");
            }
            customer.setEmail(request.getEmail());
        }

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
        log.info("Customer updated with id: {}", updatedCustomer.getId());

        return CustomerResponse.fromEntity(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.delete(customer);
        log.info("Customer deleted with id: {}", id);
    }
}
