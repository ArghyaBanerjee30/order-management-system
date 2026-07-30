package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.TestDataBuilder;
import com.orderservice.dto.CreateCustomerRequest;
import com.orderservice.dto.UpdateCustomerRequest;
import com.orderservice.entity.Customer;
import com.orderservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CustomerController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCustomerRequest createRequest;
    private UpdateCustomerRequest updateRequest;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        createRequest = TestDataBuilder.createCustomerRequest();
        updateRequest = TestDataBuilder.createUpdateCustomerRequest();
    }

    // POST /customers Tests

    @Test
    void createCustomer_ValidRequest_Returns201() throws Exception {
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value(createRequest.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(createRequest.getLastName()))
                .andExpect(jsonPath("$.email").value(createRequest.getEmail()))
                .andExpect(jsonPath("$.phone").value(createRequest.getPhone()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createCustomer_DuplicateEmail_Returns409() throws Exception {
        // Create first customer
        Customer existingCustomer = new Customer();
        existingCustomer.setFirstName("Existing");
        existingCustomer.setLastName("Customer");
        existingCustomer.setEmail(createRequest.getEmail());
        existingCustomer.setPhone("+1-555-999-9999");
        customerRepository.save(existingCustomer);

        // Try to create another with same email
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    void createCustomer_InvalidRequest_MissingFirstName_Returns400() throws Exception {
        createRequest.setFirstName(null);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("firstName")));
    }

    @Test
    void createCustomer_InvalidEmail_Returns400() throws Exception {
        createRequest.setEmail("invalid-email");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")));
    }

    @Test
    void createCustomer_InvalidPhone_Returns400() throws Exception {
        createRequest.setPhone("123"); // Too short

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("phone")));
    }

    // GET /customers Tests

    @Test
    void getAllCustomers_ReturnsListOfCustomers() throws Exception {
        // Create test customers
        Customer customer1 = TestDataBuilder.createTestCustomer(null, "customer1@example.com");
        Customer customer2 = TestDataBuilder.createTestCustomer(null, "customer2@example.com");
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].email").value(containsInAnyOrder(
                        "customer1@example.com",
                        "customer2@example.com"
                )));
    }

    @Test
    void getAllCustomers_EmptyList_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /customers/{id} Tests

    @Test
    void getCustomerById_Found_Returns200() throws Exception {
        // Create test customer
        Customer customer = TestDataBuilder.createTestCustomer(null, "test@example.com");
        customer = customerRepository.save(customer);

        mockMvc.perform(get("/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.email").value(customer.getEmail()))
                .andExpect(jsonPath("$.firstName").value(customer.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(customer.getLastName()));
    }

    @Test
    void getCustomerById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/customers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    // PUT /customers/{id} Tests

    @Test
    void updateCustomer_ValidRequest_Returns200() throws Exception {
        // Create test customer
        Customer customer = TestDataBuilder.createTestCustomer(null, "original@example.com");
        customer = customerRepository.save(customer);

        mockMvc.perform(put("/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.firstName").value(updateRequest.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(updateRequest.getLastName()))
                .andExpect(jsonPath("$.email").value(updateRequest.getEmail()))
                .andExpect(jsonPath("$.phone").value(updateRequest.getPhone()));
    }

    @Test
    void updateCustomer_PartialUpdate_Returns200() throws Exception {
        // Create test customer
        Customer customer = TestDataBuilder.createTestCustomer(null, "original@example.com");
        customer = customerRepository.save(customer);

        // Update only first name
        UpdateCustomerRequest partialRequest = new UpdateCustomerRequest();
        partialRequest.setFirstName("UpdatedName");

        mockMvc.perform(put("/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.firstName").value("UpdatedName"))
                .andExpect(jsonPath("$.email").value(customer.getEmail())); // Unchanged
    }

    @Test
    void updateCustomer_NotFound_Returns404() throws Exception {
        mockMvc.perform(put("/customers/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    @Test
    void updateCustomer_DuplicateEmail_Returns409() throws Exception {
        // Create two customers
        Customer customer1 = TestDataBuilder.createTestCustomer(null, "customer1@example.com");
        Customer customer2 = TestDataBuilder.createTestCustomer(null, "customer2@example.com");
        customer1 = customerRepository.save(customer1);
        customer2 = customerRepository.save(customer2);

        // Try to update customer1 with customer2's email
        updateRequest.setEmail("customer2@example.com");

        mockMvc.perform(put("/customers/{id}", customer1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    void updateCustomer_InvalidEmail_Returns400() throws Exception {
        // Create test customer
        Customer customer = TestDataBuilder.createTestCustomer(null, "original@example.com");
        customer = customerRepository.save(customer);

        updateRequest.setEmail("invalid-email");

        mockMvc.perform(put("/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")));
    }

    // DELETE /customers/{id} Tests

    @Test
    void deleteCustomer_Success_Returns204() throws Exception {
        // Create test customer
        Customer customer = TestDataBuilder.createTestCustomer(null, "test@example.com");
        customer = customerRepository.save(customer);

        mockMvc.perform(delete("/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        // Verify customer was deleted
        mockMvc.perform(get("/customers/{id}", customer.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/customers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }
}
