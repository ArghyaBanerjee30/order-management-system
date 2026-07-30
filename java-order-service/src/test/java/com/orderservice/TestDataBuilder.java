package com.orderservice;

import com.orderservice.dto.*;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building test data.
 */
public class TestDataBuilder {

    // Order Test Data Builders

    public static Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.valueOf(199.98));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderItem item = createTestOrderItem();
        item.setOrder(order);
        order.getOrderItems().add(item);

        return order;
    }

    public static Order createTestOrder(Long id, OrderStatus status) {
        Order order = createTestOrder();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

    public static OrderItem createTestOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(99.99));
        item.setSubtotal(BigDecimal.valueOf(199.98));
        return item;
    }

    public static CreateOrderRequest createOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);

        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(99.99));
        items.add(item);

        request.setOrderItems(items);
        return request;
    }

    public static CreateOrderRequest createOrderRequest(Long customerId) {
        CreateOrderRequest request = createOrderRequest();
        request.setCustomerId(customerId);
        return request;
    }

    // Inventory Test Data Builders

    public static InventoryReserveResponse createSuccessfulReserveResponse() {
        InventoryReserveResponse response = new InventoryReserveResponse();
        response.setSuccess(true);
        response.setMessage("Stock reserved successfully");

        InventoryDTO inventory = new InventoryDTO();
        inventory.setProductId(1L);
        inventory.setAvailableQuantity(98);
        inventory.setReservedQuantity(2);
        response.setInventory(inventory);

        return response;
    }

    public static InventoryReleaseResponse createSuccessfulReleaseResponse() {
        InventoryReleaseResponse response = new InventoryReleaseResponse();
        response.setSuccess(true);
        response.setMessage("Stock released successfully");

        InventoryDTO inventory = new InventoryDTO();
        inventory.setProductId(1L);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);
        response.setInventory(inventory);

        return response;
    }
}
