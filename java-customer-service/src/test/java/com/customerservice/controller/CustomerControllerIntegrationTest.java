package com.customerservice.controller;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.UpdateCustomerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCustomer_Success() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1-555-123-4567"
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createCustomer_DuplicateEmail() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Jane",
                "Smith",
                "duplicate@example.com",
                "+1-555-987-6543"
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCustomer_ValidationError() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "",
                "Doe",
                "invalid-email",
                "invalid-phone"
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getCustomerById_Success() throws Exception {
        CreateCustomerRequest createRequest = new CreateCustomerRequest(
                "Alice",
                "Johnson",
                "alice.johnson@example.com",
                "+1-555-111-2222"
        );

        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long customerId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.email").value("alice.johnson@example.com"));
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        mockMvc.perform(get("/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateCustomer_Success() throws Exception {
        CreateCustomerRequest createRequest = new CreateCustomerRequest(
                "Bob",
                "Williams",
                "bob.williams@example.com",
                "+1-555-333-4444"
        );

        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long customerId = objectMapper.readTree(responseBody).get("id").asLong();

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Robert",
                "Williams",
                "robert.williams@example.com",
                "+1-555-555-6666"
        );

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.email").value("robert.williams@example.com"));
    }

    @Test
    void updateCustomer_NotFound() throws Exception {
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Test",
                "User",
                "test@example.com",
                "+1-555-777-8888"
        );

        mockMvc.perform(put("/customers/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        CreateCustomerRequest createRequest = new CreateCustomerRequest(
                "Charlie",
                "Brown",
                "charlie.brown@example.com",
                "+1-555-999-0000"
        );

        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long customerId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(delete("/customers/{id}", customerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_NotFound() throws Exception {
        mockMvc.perform(delete("/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
