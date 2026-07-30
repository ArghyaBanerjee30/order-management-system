package com.orderservice.service;

import com.orderservice.TestDataBuilder;
import com.orderservice.dto.CreateCustomerRequest;
import com.orderservice.dto.CustomerResponse;
import com.orderservice.dto.UpdateCustomerRequest;
import com.orderservice.entity.Customer;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.DuplicateCustomerException;
import com.orderservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService.
 */
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
        testCustomer = TestDataBuilder.createTestCustomer();
        createRequest = TestDataBuilder.createCustomerRequest();
        updateRequest = TestDataBuilder.createUpdateCustomerRequest();
    }

    // Create Customer Tests

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerResponse response = customerService.createCustomer(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testCustomer.getId());
        assertThat(response.getEmail()).isEqualTo(testCustomer.getEmail());
        assertThat(response.getFirstName()).isEqualTo(testCustomer.getFirstName());
        assertThat(response.getLastName()).isEqualTo(testCustomer.getLastName());

        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        when(customerRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> customerService.createCustomer(createRequest))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining("Customer with email");

        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // Get Customer By ID Tests

    @Test
    void getCustomerById_Found() {
        // Given
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        // When
        CustomerResponse response = customerService.getCustomerById(customerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testCustomer.getId());
        assertThat(response.getEmail()).isEqualTo(testCustomer.getEmail());

        verify(customerRepository).findById(customerId);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.getCustomerById(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 999");

        verify(customerRepository).findById(customerId);
    }

    // Get All Customers Tests

    @Test
    void getAllCustomers_ReturnsListOfCustomers() {
        // Given
        Customer customer2 = TestDataBuilder.createTestCustomer(2L, "jane.doe@example.com");
        when(customerRepository.findAll()).thenReturn(Arrays.asList(testCustomer, customer2));

        // When
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(testCustomer.getId());
        assertThat(responses.get(1).getId()).isEqualTo(customer2.getId());

        verify(customerRepository).findAll();
    }

    @Test
    void getAllCustomers_EmptyList() {
        // Given
        when(customerRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Then
        assertThat(responses).isEmpty();

        verify(customerRepository).findAll();
    }

    // Update Customer Tests

    @Test
    void updateCustomer_Success_AllFields() {
        // Given
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerResponse response = customerService.updateCustomer(customerId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(customerRepository).findById(customerId);
        verify(customerRepository).existsByEmail(updateRequest.getEmail());
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void updateCustomer_Success_PartialUpdate() {
        // Given
        Long customerId = 1L;
        UpdateCustomerRequest partialRequest = new UpdateCustomerRequest();
        partialRequest.setFirstName("UpdatedName");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerResponse response = customerService.updateCustomer(customerId, partialRequest);

        // Then
        assertThat(response).isNotNull();
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.updateCustomer(customerId, updateRequest))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 999");

        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicateEmail_ThrowsException() {
        // Given
        Long customerId = 1L;
        Customer otherCustomer = TestDataBuilder.createTestCustomer(2L, updateRequest.getEmail());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);
        when(customerRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.of(otherCustomer));

        // When & Then
        assertThatThrownBy(() -> customerService.updateCustomer(customerId, updateRequest))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining("Customer with email");

        verify(customerRepository).findById(customerId);
        verify(customerRepository).existsByEmail(updateRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_SameEmail_Success() {
        // Given
        Long customerId = 1L;
        updateRequest.setEmail(testCustomer.getEmail()); // Same email as existing

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);
        when(customerRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerResponse response = customerService.updateCustomer(customerId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(testCustomer);
    }

    // Delete Customer Tests

    @Test
    void deleteCustomer_Success() {
        // Given
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).delete(testCustomer);

        // When
        customerService.deleteCustomer(customerId);

        // Then
        verify(customerRepository).findById(customerId);
        verify(customerRepository).delete(testCustomer);
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.deleteCustomer(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 999");

        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
