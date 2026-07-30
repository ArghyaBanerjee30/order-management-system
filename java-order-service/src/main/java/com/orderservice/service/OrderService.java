package com.orderservice.service;

import com.orderservice.client.CustomerClient;
import com.orderservice.constants.OrderConstants;
import com.orderservice.dto.CreateOrderRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.CustomerServiceException;
import com.orderservice.exception.InvalidOrderStatusException;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.mapper.OrderMapper;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final InventoryClient inventoryClient;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        validateCustomer(request.getCustomerId());
        Order order = createDraftOrder(request);

        try {
            reserveInventoryForOrder(order);
            order = confirmOrder(order);
        } catch (Exception e) {
            rollbackInventoryReservation(order);
            order = markOrderAsFailed(order);
        }

        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return findOrderById(orderId)
                .map(OrderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        validateCustomer(customerId);
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStatusException(orderId, order.getStatus(), OrderConstants.OPERATION_CANCEL);
        }

        order.getOrderItems().forEach(this::safeReleaseStock);
        order.setStatus(OrderStatus.CANCELLED);
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private void validateCustomer(Long customerId) {
        try {
            customerClient.getCustomer(customerId);
        } catch (CustomerNotFoundException | CustomerServiceException e) {
            throw e;
        }
    }

    private Order createDraftOrder(CreateOrderRequest request) {
        Order order = OrderMapper.toDraftOrder(request);

        List<OrderItem> items = request.getOrderItems().stream()
                .map(OrderMapper::toOrderItem)
                .peek(item -> order.addOrderItem(item))
                .collect(Collectors.toList());

        order.setTotalAmount(OrderMapper.calculateTotalAmount(items));
        return orderRepository.save(order);
    }

    private void reserveInventoryForOrder(Order order) {
        order.getOrderItems().forEach(item ->
            inventoryClient.reserveStock(item.getProductId(), item.getQuantity())
        );
    }

    private void rollbackInventoryReservation(Order order) {
        order.getOrderItems().forEach(this::safeReleaseStock);
    }

    private void safeReleaseStock(OrderItem item) {
        try {
            inventoryClient.releaseStock(item.getProductId(), item.getQuantity());
        } catch (Exception ignored) {
        }
    }

    private Order confirmOrder(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    private Order markOrderAsFailed(Order order) {
        order.setStatus(OrderStatus.FAILED);
        return orderRepository.save(order);
    }

    private Optional<Order> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
