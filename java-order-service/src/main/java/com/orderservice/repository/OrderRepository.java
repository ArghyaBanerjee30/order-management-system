package com.orderservice.repository;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Order entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific customer.
     *
     * @param customerId the customer ID
     * @return list of orders for the customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Find all orders with a specific status.
     *
     * @param status the order status
     * @return list of orders with the given status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all orders for a customer with a specific status.
     *
     * @param customerId the customer ID
     * @param status the order status
     * @return list of orders matching both criteria
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
}
