package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.TestDataBuilder;
import com.orderservice.client.CustomerClient;
import com.orderservice.client.InventoryClient;
import com.orderservice.dto.CreateOrderRequest;
import com.orderservice.dto.CustomerResponse;
import com.orderservice.dto.InventoryReleaseResponse;
import com.orderservice.dto.InventoryReserveResponse;
import com.orderservice.dto.OrderItemRequest;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private CustomerClient customerClient;

    private Long testCustomerId;
    private CreateOrderRequest createOrderRequest;
    private InventoryReserveResponse successfulReserveResponse;
    private InventoryReleaseResponse successfulReleaseResponse;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        // Use a simple test customer ID
        testCustomerId = 1L;

        // Mock customer client to return test customer data
        CustomerResponse mockCustomerResponse = new CustomerResponse(
                testCustomerId,
                "John",
                "Doe",
                "test@example.com",
                "+1-555-0000",
                null
        );
        when(customerClient.getCustomer(testCustomerId)).thenReturn(mockCustomerResponse);

        // Create order request
        createOrderRequest = TestDataBuilder.createOrderRequest(testCustomerId);

        // Setup inventory responses
        successfulReserveResponse = TestDataBuilder.createSuccessfulReserveResponse();
        successfulReleaseResponse = TestDataBuilder.createSuccessfulReleaseResponse();
    }

    // POST /orders Tests

    @Test
    void createOrder_ValidRequest_Returns201() throws Exception {
        // Mock inventory service
        when(inventoryClient.reserveStock(any())).thenReturn(successfulReserveResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(testCustomerId))
                .andExpect(jsonPath("$.status").value(OrderStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createOrder_CustomerNotFound_Returns404() throws Exception {
        createOrderRequest.setCustomerId(999L);
        when(customerClient.getCustomer(999L)).thenThrow(new com.orderservice.exception.CustomerNotFoundException(999L));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    @Test
    void createOrder_InvalidRequest_MissingCustomerId_Returns400() throws Exception {
        createOrderRequest.setCustomerId(null);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("customerId")));
    }

    @Test
    void createOrder_InvalidRequest_EmptyOrderItems_Returns400() throws Exception {
        createOrderRequest.setOrderItems(new ArrayList<>());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("orderItems")));
    }

    @Test
    void createOrder_InvalidRequest_InvalidQuantity_Returns400() throws Exception {
        OrderItemRequest invalidItem = new OrderItemRequest();
        invalidItem.setProductId(1L);
        invalidItem.setQuantity(0); // Invalid - must be >= 1
        invalidItem.setPrice(BigDecimal.valueOf(99.99));
        createOrderRequest.getOrderItems().clear();
        createOrderRequest.getOrderItems().add(invalidItem);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("orderItems[0].quantity")));
    }

    @Test
    void createOrder_InvalidRequest_InvalidPrice_Returns400() throws Exception {
        OrderItemRequest invalidItem = new OrderItemRequest();
        invalidItem.setProductId(1L);
        invalidItem.setQuantity(1);
        invalidItem.setPrice(BigDecimal.valueOf(0.0)); // Invalid - must be > 0.01
        createOrderRequest.getOrderItems().clear();
        createOrderRequest.getOrderItems().add(invalidItem);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createOrder_InsufficientStock_OrderMarkedAsFailed() throws Exception {
        // Mock inventory service to return failure
        InventoryReserveResponse failedResponse = new InventoryReserveResponse();
        failedResponse.setSuccess(false);
        failedResponse.setMessage("Insufficient stock");
        when(inventoryClient.reserveStock(any())).thenReturn(failedResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(OrderStatus.FAILED.toString()));
    }

    // GET /orders Tests

    @Test
    void getAllOrders_ReturnsListOfOrders() throws Exception {
        // Create test orders
        when(inventoryClient.reserveStock(any())).thenReturn(successfulReserveResponse);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getAllOrders_EmptyList_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /orders/{id} Tests

    @Test
    void getOrderById_Found_Returns200() throws Exception {
        // Create test order
        when(inventoryClient.reserveStock(any())).thenReturn(successfulReserveResponse);

        String createResponse = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerId").value(testCustomerId))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    void getOrderById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Order not found")));
    }

    // GET /orders/customer/{customerId} Tests

    @Test
    void getOrdersByCustomerId_ReturnsCustomerOrders() throws Exception {
        // Create test order
        when(inventoryClient.reserveStock(any())).thenReturn(successfulReserveResponse);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)));

        mockMvc.perform(get("/orders/customer/{customerId}", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].customerId").value(testCustomerId));
    }

    @Test
    void getOrdersByCustomerId_CustomerNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/orders/customer/{customerId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    @Test
    void getOrdersByCustomerId_NoOrders_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/orders/customer/{customerId}", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // POST /orders/{id}/cancel Tests

    @Test
    void cancelOrder_Success_Returns200() throws Exception {
        // Create test order
        when(inventoryClient.reserveStock(any())).thenReturn(successfulReserveResponse);
        when(inventoryClient.releaseStock(any())).thenReturn(successfulReleaseResponse);

        String createResponse = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(post("/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value(OrderStatus.CANCELLED.toString()));
    }

    @Test
    void cancelOrder_OrderNotFound_Returns404() throws Exception {
        mockMvc.perform(post("/orders/{id}/cancel", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Order not found")));
    }

    @Test
    void cancelOrder_InvalidStatus_Returns400() throws Exception {
        // Create a draft order manually (bypass inventory reservation)
        Order draftOrder = TestDataBuilder.createTestOrder();
        draftOrder.setCustomerId(testCustomerId);
        draftOrder.setStatus(OrderStatus.DRAFT);
        draftOrder = orderRepository.save(draftOrder);

        mockMvc.perform(post("/orders/{id}/cancel", draftOrder.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Cannot cancel order")));
    }

    @Test
    void cancelOrder_AlreadyCancelled_Returns400() throws Exception {
        // Create and cancel order
        when(inventoryClient.reserveStock(any())).thenReturn(successfulReserveResponse);
        when(inventoryClient.releaseStock(any())).thenReturn(successfulReleaseResponse);

        String createResponse = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // Cancel once
        mockMvc.perform(post("/orders/{id}/cancel", orderId))
                .andExpect(status().isOk());

        // Try to cancel again
        mockMvc.perform(post("/orders/{id}/cancel", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Cannot cancel order")));
    }
}
