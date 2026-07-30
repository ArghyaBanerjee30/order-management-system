package com.orderservice.service;

import com.orderservice.client.CustomerClient;
import com.orderservice.dto.CreateOrderRequest;
import com.orderservice.dto.OrderItemDTO;
import com.orderservice.dto.OrderItemRequest;
import com.orderservice.dto.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.CustomerServiceException;
import com.orderservice.exception.InvalidOrderStatusException;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.exception.OrderValidationException;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Order business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final InventoryClient inventoryClient;

    /**
     * Validate that customer exists.
     *
     * @param customerId customer ID
     * @throws CustomerNotFoundException if customer not found
     * @throws CustomerServiceException if customer service is unavailable
     */
    private void validateCustomer(Long customerId) {
        try {
            customerClient.getCustomer(customerId);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", customerId);
            throw e;
        } catch (CustomerServiceException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Create a draft order without confirming inventory.
     *
     * @param request order creation request
     * @return draft order
     */
    @Transactional
    public Order createDraftOrder(CreateOrderRequest request) {
        log.info("Creating draft order for customer {}", request.getCustomerId());

        // Validate customer exists
        validateCustomer(request.getCustomerId());

        // Create order entity
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.ZERO);

        // Create order items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.calculateSubtotal();

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Draft order created with id: {}", savedOrder.getId());

        return savedOrder;
    }

    /**
     * Reserve inventory for all items in the order.
     *
     * @param order order to reserve inventory for
     * @return true if all reservations successful
     * @throws Exception if any reservation fails
     */
    private boolean reserveInventoryForOrder(Order order) {
        log.info("Reserving inventory for order {}", order.getId());

        try {
            for (OrderItem item : order.getOrderItems()) {
                log.debug("Reserving {} units of product {} for order {}",
                        item.getQuantity(), item.getProductId(), order.getId());

                inventoryClient.reserveStock(item.getProductId(), item.getQuantity());

                log.debug("Successfully reserved {} units of product {}",
                        item.getQuantity(), item.getProductId());
            }

            log.info("All inventory reserved successfully for order {}", order.getId());
            return true;

        } catch (Exception e) {
            log.error("Failed to reserve inventory for order {}: {}",
                    order.getId(), e.getMessage());

            // Rollback: release any successfully reserved items
            rollbackInventoryReservation(order);
            throw e;
        }
    }

    /**
     * Rollback inventory reservations for an order.
     * Used when reservation fails partway through.
     *
     * @param order order to rollback reservations for
     */
    private void rollbackInventoryReservation(Order order) {
        log.warn("Rolling back inventory reservations for order {}", order.getId());

        for (OrderItem item : order.getOrderItems()) {
            try {
                inventoryClient.releaseStock(item.getProductId(), item.getQuantity());
                log.debug("Released {} units of product {} during rollback",
                        item.getQuantity(), item.getProductId());
            } catch (Exception e) {
                log.error("Failed to release stock during rollback for product {}: {}",
                        item.getProductId(), e.getMessage());
                // Continue with rollback for other items
            }
        }
    }

    /**
     * Confirm an order after successful inventory reservation.
     * Updates order status from DRAFT to CONFIRMED.
     *
     * @param order order to confirm
     * @return confirmed order
     */
    @Transactional
    public Order confirmOrder(Order order) {
        log.info("Confirming order {}", order.getId());

        order.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(order);

        log.info("Order {} confirmed successfully", confirmedOrder.getId());
        return confirmedOrder;
    }

    /**
     * Mark an order as failed when inventory reservation fails.
     * Updates order status from DRAFT to FAILED.
     *
     * @param order order to mark as failed
     * @return failed order
     */
    @Transactional
    public Order markOrderAsFailed(Order order) {
        log.warn("Marking order {} as FAILED", order.getId());

        order.setStatus(OrderStatus.FAILED);
        Order failedOrder = orderRepository.save(order);

        log.info("Order {} marked as FAILED", failedOrder.getId());
        return failedOrder;
    }

    /**
     * Create and process a complete order.
     * Orchestrates the entire order creation flow:
     * 1. Validate customer
     * 2. Create draft order
     * 3. Reserve inventory
     * 4. If successful, confirm order
     * 5. If failed, mark order as failed
     *
     * @param request order creation request
     * @return processed order (CONFIRMED or FAILED)
     * @throws CustomerNotFoundException if customer not found
     * @throws CustomerServiceException if customer service is unavailable
     * @throws OrderValidationException if order validation fails
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Starting order creation process for customer {}", request.getCustomerId());

        Order order = null;

        try {
            // Step 1: Validate customer
            validateCustomer(request.getCustomerId());

            // Step 2: Create draft order
            order = createDraftOrder(request);
            log.info("Order {} created in DRAFT status", order.getId());

            // Step 3: Reserve inventory for all items
            reserveInventoryForOrder(order);
            log.info("Inventory reserved successfully for order {}", order.getId());

            // Step 4: Confirm order
            order = confirmOrder(order);
            log.info("Order {} CONFIRMED successfully", order.getId());

            return mapToResponse(order);

        } catch (CustomerNotFoundException | CustomerServiceException e) {
            // Customer validation failed - no order was created
            log.error("Order creation failed: {}", e.getMessage(), e);
            throw e;

        } catch (Exception e) {
            // Inventory reservation or other error - mark order as failed
            log.error("Order creation failed for order {}: {}",
                    order != null ? order.getId() : "N/A", e.getMessage(), e);

            if (order != null) {
                order = markOrderAsFailed(order);
                log.warn("Order {} marked as FAILED due to: {}", order.getId(), e.getMessage());
                return mapToResponse(order);
            }

            // If order creation itself failed, wrap in validation exception
            throw new OrderValidationException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Map Order entity to OrderResponse DTO.
     *
     * @param order order entity
     * @return order response DTO
     */
    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Map order items
        response.setOrderItems(
                order.getOrderItems().stream()
                        .map(this::mapItemToDTO)
                        .collect(Collectors.toList())
        );

        return response;
    }

    /**
     * Map OrderItem entity to OrderItemDTO.
     *
     * @param item order item entity
     * @return order item DTO
     */
    private OrderItemDTO mapItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    /**
     * Get an order by ID.
     *
     * @param orderId order ID
     * @return order response DTO
     * @throws OrderNotFoundException if order not found
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.debug("Fetching order with id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", orderId);
                    return new OrderNotFoundException(orderId);
                });

        log.debug("Order {} found with status: {}", orderId, order.getStatus());
        return mapToResponse(order);
    }

    /**
     * Get all orders.
     *
     * @return list of order response DTOs
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders");

        List<Order> orders = orderRepository.findAll();
        log.debug("Found {} orders", orders.size());

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders for a specific customer.
     *
     * @param customerId customer ID
     * @return list of order response DTOs
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        log.debug("Fetching orders for customer: {}", customerId);

        // Validate customer exists
        validateCustomer(customerId);

        List<Order> orders = orderRepository.findByCustomerId(customerId);
        log.debug("Found {} orders for customer {}", orders.size(), customerId);

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an order.
     * Releases reserved inventory and updates order status to CANCELLED.
     *
     * @param orderId order ID
     * @return cancelled order response DTO
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderStatusException if order is not in CONFIRMED status
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Attempting to cancel order {}", orderId);

        // Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Cannot cancel order: order {} not found", orderId);
                    return new OrderNotFoundException(orderId);
                });

        // Validate order status - only CONFIRMED orders can be cancelled
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            log.error("Cannot cancel order {}: current status is {}, expected CONFIRMED",
                    orderId, order.getStatus());
            throw new InvalidOrderStatusException(orderId, order.getStatus(), "cancel");
        }

        // Release inventory for all items
        try {
            for (OrderItem item : order.getOrderItems()) {
                log.debug("Releasing {} units of product {} for order {}",
                        item.getQuantity(), item.getProductId(), orderId);

                inventoryClient.releaseStock(item.getProductId(), item.getQuantity());

                log.debug("Successfully released {} units of product {}",
                        item.getQuantity(), item.getProductId());
            }

            log.info("All inventory released successfully for order {}", orderId);

        } catch (Exception e) {
            log.error("Failed to release inventory for order {}: {}",
                    orderId, e.getMessage(), e);
            // Log the error but still proceed with cancellation
            // The order should be marked as cancelled even if release fails
            // Manual inventory adjustment may be needed
            log.warn("Order {} will be cancelled, but inventory release failed. Manual adjustment may be needed.",
                    orderId);
        }

        // Update order status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully", orderId);
        return mapToResponse(cancelledOrder);
    }
}
