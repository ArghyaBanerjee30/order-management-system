package com.customerservice.service;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.entity.Customer;
import com.customerservice.exception.CustomerNotFoundException;
import com.customerservice.exception.DuplicateCustomerException;
import com.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CreateCustomerRequest createRequest;
    private UpdateCustomerRequest updateRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhone("+1-555-123-4567");
        testCustomer.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1-555-123-4567"
        );

        updateRequest = new UpdateCustomerRequest(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "+1-555-987-6543"
        );
    }

    @Test
    void createCustomer_Success() {
        when(customerRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.createCustomer(createRequest);

        assertNotNull(response);
        assertEquals(testCustomer.getId(), response.getId());
        assertEquals(testCustomer.getEmail(), response.getEmail());
        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail() {
        when(customerRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> {
            customerService.createCustomer(createRequest);
        });

        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(testCustomer.getId(), response.getId());
        assertEquals(testCustomer.getEmail(), response.getEmail());
        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById(999L);
        });

        verify(customerRepository).findById(999L);
    }

    @Test
    void getAllCustomers_Success() {
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setPhone("+1-555-987-6543");
        customer2.setCreatedAt(LocalDateTime.now());

        when(customerRepository.findAll()).thenReturn(Arrays.asList(testCustomer, customer2));

        List<CustomerResponse> responses = customerService.getAllCustomers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(customerRepository).findAll();
    }

    @Test
    void updateCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        assertNotNull(response);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.updateCustomer(999L, updateRequest);
        });

        verify(customerRepository).findById(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicateEmail() {
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setEmail(updateRequest.getEmail());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> {
            customerService.updateCustomer(1L, updateRequest);
        });

        verify(customerRepository).findById(1L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).delete(testCustomer);

        customerService.deleteCustomer(1L);

        verify(customerRepository).findById(1L);
        verify(customerRepository).delete(testCustomer);
    }

    @Test
    void deleteCustomer_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer(999L);
        });

        verify(customerRepository).findById(999L);
        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
