package com.orderservice.entity;

import com.orderservice.constants.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = ValidationMessages.ORDER_REQUIRED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = ValidationMessages.PRODUCT_ID_REQUIRED)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull(message = ValidationMessages.QUANTITY_REQUIRED)
    @Min(value = 1, message = ValidationMessages.QUANTITY_MIN)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = ValidationMessages.PRICE_REQUIRED)
    @DecimalMin(value = "0.0", inclusive = true, message = ValidationMessages.PRICE_NON_NEGATIVE)
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = ValidationMessages.SUBTOTAL_REQUIRED)
    @DecimalMin(value = "0.0", inclusive = true, message = ValidationMessages.SUBTOTAL_NON_NEGATIVE)
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public void calculateSubtotal() {
        this.subtotal = Optional.ofNullable(quantity)
                .flatMap(q -> Optional.ofNullable(price).map(p -> p.multiply(BigDecimal.valueOf(q))))
                .orElse(BigDecimal.ZERO);
    }
}
