package com.orderservice.service;

import com.orderservice.TestDataBuilder;
import com.orderservice.client.CustomerClient;
import com.orderservice.client.InventoryClient;
import com.orderservice.dto.*;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.CustomerServiceException;
import com.orderservice.exception.InvalidOrderStatusException;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.repository.OrderRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    private CustomerResponse testCustomerResponse;
    private Order testOrder;
    private CreateOrderRequest createOrderRequest;
    private InventoryReserveResponse successfulReserveResponse;
    private InventoryReleaseResponse successfulReleaseResponse;

    @BeforeEach
    void setUp() {
        testCustomerResponse = new CustomerResponse(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1-555-123-4567",
                null
        );
        testOrder = TestDataBuilder.createTestOrder();
        createOrderRequest = TestDataBuilder.createOrderRequest();
        successfulReserveResponse = TestDataBuilder.createSuccessfulReserveResponse();
        successfulReleaseResponse = TestDataBuilder.createSuccessfulReleaseResponse();
    }

    // Create Order Tests - Success

    @Test
    void createOrder_Success_FullFlow() {
        // Given
        when(customerClient.getCustomer(createOrderRequest.getCustomerId())).thenReturn(testCustomerResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        when(inventoryClient.reserveStock(any(InventoryReserveRequest.class))).thenReturn(successfulReserveResponse);

        // When
        OrderResponse response = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo(createOrderRequest.getCustomerId());
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getOrderItems()).hasSize(1);

        verify(customerClient).getCustomer(createOrderRequest.getCustomerId());
        verify(orderRepository, times(2)).save(any(Order.class)); // Draft + Confirmed
        verify(inventoryClient).reserveStock(any(InventoryReserveRequest.class));
    }

    // Create Order Tests - Customer Not Found

    @Test
    void createOrder_CustomerNotFound_ThrowsException() {
        // Given
        when(customerClient.getCustomer(createOrderRequest.getCustomerId()))
                .thenThrow(new CustomerNotFoundException(createOrderRequest.getCustomerId()));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id:");

        verify(customerClient).getCustomer(createOrderRequest.getCustomerId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryClient, never()).reserveStock(any(InventoryReserveRequest.class));
    }

    @Test
    void createOrder_CustomerServiceUnavailable_ThrowsException() {
        // Given
        when(customerClient.getCustomer(createOrderRequest.getCustomerId()))
                .thenThrow(new CustomerServiceException("Customer service is unavailable"));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(CustomerServiceException.class)
                .hasMessageContaining("Customer service is unavailable");

        verify(customerClient).getCustomer(createOrderRequest.getCustomerId());
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryClient, never()).reserveStock(any(InventoryReserveRequest.class));
    }

    // Create Order Tests - Inventory Reservation Failed

    @Test
    void createOrder_InventoryReservationFailed_OrderMarkedAsFailed() {
        // Given
        when(customerClient.getCustomer(createOrderRequest.getCustomerId())).thenReturn(testCustomerResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        when(inventoryClient.reserveStock(any(InventoryReserveRequest.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        // When
        OrderResponse response = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FAILED);

        verify(customerClient).getCustomer(createOrderRequest.getCustomerId());
        verify(orderRepository, times(2)).save(any(Order.class)); // Draft + Failed
        verify(inventoryClient).reserveStock(any(InventoryReserveRequest.class));
    }

    @Test
    void createOrder_InsufficientStock_OrderMarkedAsFailed() {
        // Given
        InventoryReserveResponse failedResponse = new InventoryReserveResponse();
        failedResponse.setSuccess(false);
        failedResponse.setMessage("Insufficient stock");

        when(customerClient.getCustomer(createOrderRequest.getCustomerId())).thenReturn(testCustomerResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        when(inventoryClient.reserveStock(any(InventoryReserveRequest.class))).thenReturn(failedResponse);

        // When
        OrderResponse response = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FAILED);

        verify(inventoryClient).reserveStock(any(InventoryReserveRequest.class));
    }

    // Get Order By ID Tests

    @Test
    void getOrderById_Found() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        OrderResponse response = orderService.getOrderById(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testOrder.getId());
        assertThat(response.getCustomerId()).isEqualTo(testOrder.getCustomerId());
        assertThat(response.getStatus()).isEqualTo(testOrder.getStatus());

        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: 999");

        verify(orderRepository).findById(orderId);
    }

    // Get All Orders Tests

    @Test
    void getAllOrders_ReturnsListOfOrders() {
        // Given
        Order order2 = TestDataBuilder.createTestOrder(2L, OrderStatus.CONFIRMED);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder, order2));

        // When
        List<OrderResponse> responses = orderService.getAllOrders();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(testOrder.getId());
        assertThat(responses.get(1).getId()).isEqualTo(order2.getId());

        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrders_EmptyList() {
        // Given
        when(orderRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<OrderResponse> responses = orderService.getAllOrders();

        // Then
        assertThat(responses).isEmpty();

        verify(orderRepository).findAll();
    }

    // Get Orders By Customer ID Tests

    @Test
    void getOrdersByCustomerId_CustomerExists() {
        // Given
        Long customerId = 1L;
        when(customerClient.getCustomer(customerId)).thenReturn(testCustomerResponse);
        when(orderRepository.findByCustomerId(customerId)).thenReturn(Arrays.asList(testOrder));

        // When
        List<OrderResponse> responses = orderService.getOrdersByCustomerId(customerId);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCustomerId()).isEqualTo(customerId);

        verify(customerClient).getCustomer(customerId);
        verify(orderRepository).findByCustomerId(customerId);
    }

    @Test
    void getOrdersByCustomerId_CustomerNotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(customerClient.getCustomer(customerId))
                .thenThrow(new CustomerNotFoundException(customerId));

        // When & Then
        assertThatThrownBy(() -> orderService.getOrdersByCustomerId(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 999");

        verify(customerClient).getCustomer(customerId);
        verify(orderRepository, never()).findByCustomerId(anyLong());
    }

    // Cancel Order Tests

    @Test
    void cancelOrder_Success() {
        // Given
        Long orderId = 1L;
        Order confirmedOrder = TestDataBuilder.createTestOrder(orderId, OrderStatus.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(confirmedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);
        when(inventoryClient.releaseStock(any(InventoryReleaseRequest.class))).thenReturn(successfulReleaseResponse);

        // When
        OrderResponse response = orderService.cancelOrder(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).findById(orderId);
        verify(inventoryClient).releaseStock(any(InventoryReleaseRequest.class));
        verify(orderRepository).save(confirmedOrder);
    }

    @Test
    void cancelOrder_OrderNotFound_ThrowsException() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: 999");

        verify(orderRepository).findById(orderId);
        verify(inventoryClient, never()).releaseStock(any(InventoryReleaseRequest.class));
    }

    @Test
    void cancelOrder_InvalidStatus_ThrowsException() {
        // Given
        Long orderId = 1L;
        Order draftOrder = TestDataBuilder.createTestOrder(orderId, OrderStatus.DRAFT);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(draftOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Cannot cancel order")
                .hasMessageContaining("DRAFT");

        verify(orderRepository).findById(orderId);
        verify(inventoryClient, never()).releaseStock(any(InventoryReleaseRequest.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_AlreadyCancelled_ThrowsException() {
        // Given
        Long orderId = 1L;
        Order cancelledOrder = TestDataBuilder.createTestOrder(orderId, OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(cancelledOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Cannot cancel order")
                .hasMessageContaining("CANCELLED");

        verify(orderRepository).findById(orderId);
        verify(inventoryClient, never()).releaseStock(any(InventoryReleaseRequest.class));
    }

    @Test
    void cancelOrder_InventoryReleaseFailed_OrderStillCancelled() {
        // Given
        Long orderId = 1L;
        Order confirmedOrder = TestDataBuilder.createTestOrder(orderId, OrderStatus.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(confirmedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);
        when(inventoryClient.releaseStock(any(InventoryReleaseRequest.class)))
                .thenThrow(new RuntimeException("Inventory service unavailable"));

        // When
        OrderResponse response = orderService.cancelOrder(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).findById(orderId);
        verify(inventoryClient).releaseStock(any(InventoryReleaseRequest.class));
        verify(orderRepository).save(confirmedOrder);
    }
}
