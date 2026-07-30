package com.orderservice.mapper;

import com.orderservice.dto.CreateOrderRequest;
import com.orderservice.dto.OrderItemDTO;
import com.orderservice.dto.OrderItemRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class OrderMapper {

    private OrderMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Order toDraftOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    public static OrderItem toOrderItem(OrderItemRequest request) {
        OrderItem item = new OrderItem();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setPrice(request.getPrice());
        item.calculateSubtotal();
        return item;
    }

    public static OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setOrderItems(toOrderItemDTOs(order.getOrderItems()));
        return response;
    }

    public static OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    public static List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> items) {
        return items.stream()
                .map(OrderMapper::toOrderItemDTO)
                .collect(Collectors.toList());
    }

    public static BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
