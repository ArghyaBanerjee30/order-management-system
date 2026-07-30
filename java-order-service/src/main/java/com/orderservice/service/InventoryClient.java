package com.orderservice.service;

import com.orderservice.dto.*;
import com.orderservice.exception.InsufficientInventoryException;
import com.orderservice.exception.InventoryReservationException;
import com.orderservice.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with the Inventory Service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Qualifier("inventoryServiceUrl")
    private final String inventoryServiceUrl;

    /**
     * Reserve stock in the inventory service.
     *
     * @param productId product ID
     * @param quantity quantity to reserve
     * @return reservation response
     * @throws RuntimeException if service is unavailable or reservation fails
     */
    public InventoryReserveResponse reserveStock(Long productId, Integer quantity) {
        String url = inventoryServiceUrl + "/inventory/reserve";

        InventoryReserveRequest request = new InventoryReserveRequest(productId, quantity);

        log.info("Calling inventory service to reserve {} units of product {}", quantity, productId);

        try {
            ResponseEntity<InventoryReserveResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    InventoryReserveResponse.class
            );

            InventoryReserveResponse body = response.getBody();

            if (body != null && body.isSuccess()) {
                log.info("Successfully reserved {} units of product {}", quantity, productId);
                return body;
            } else {
                log.error("Failed to reserve stock: {}", body != null ? body.getMessage() : "Unknown error");
                throw new InventoryReservationException(body != null ? body.getMessage() :
                        "Inventory reservation failed");
            }

        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Bad request when reserving stock: {}", e.getMessage());
            throw new InsufficientInventoryException("Insufficient stock for product " + productId);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Product not found in inventory: {}", productId);
            throw new InventoryReservationException("Product not found in inventory: " + productId);
        } catch (ResourceAccessException e) {
            log.error("Inventory service is unavailable: {}", e.getMessage());
            throw new InventoryServiceException("Inventory service is unavailable", e);
        } catch (Exception e) {
            log.error("Error communicating with inventory service: {}", e.getMessage());
            throw new InventoryServiceException("Error communicating with inventory service", e);
        }
    }

    /**
     * Release reserved stock in the inventory service.
     *
     * @param productId product ID
     * @param quantity quantity to release
     * @return release response
     * @throws RuntimeException if service is unavailable or release fails
     */
    public InventoryReleaseResponse releaseStock(Long productId, Integer quantity) {
        String url = inventoryServiceUrl + "/inventory/release";

        InventoryReleaseRequest request = new InventoryReleaseRequest(productId, quantity);

        log.info("Calling inventory service to release {} units of product {}", quantity, productId);

        try {
            ResponseEntity<InventoryReleaseResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    InventoryReleaseResponse.class
            );

            InventoryReleaseResponse body = response.getBody();

            if (body != null && body.isSuccess()) {
                log.info("Successfully released {} units of product {}", quantity, productId);
                return body;
            } else {
                log.error("Failed to release stock: {}", body != null ? body.getMessage() : "Unknown error");
                throw new InventoryServiceException("Inventory release failed: " +
                        (body != null ? body.getMessage() : "Unknown error"));
            }

        } catch (ResourceAccessException e) {
            log.error("Inventory service is unavailable: {}", e.getMessage());
            throw new InventoryServiceException("Inventory service is unavailable", e);
        } catch (Exception e) {
            log.error("Error communicating with inventory service: {}", e.getMessage());
            throw new InventoryServiceException("Error communicating with inventory service", e);
        }
    }

    /**
     * Check inventory availability for a product.
     *
     * @param productId product ID
     * @return inventory DTO
     * @throws RuntimeException if service is unavailable or product not found
     */
    public InventoryDTO checkInventory(Long productId) {
        String url = inventoryServiceUrl + "/inventory/" + productId;

        log.info("Checking inventory for product {}", productId);

        try {
            ResponseEntity<InventoryDTO> response = restTemplate.getForEntity(
                    url,
                    InventoryDTO.class
            );

            InventoryDTO body = response.getBody();

            if (body != null) {
                log.info("Inventory check successful for product {}: available={}, reserved={}",
                        productId, body.getAvailableQuantity(), body.getReservedQuantity());
                return body;
            } else {
                throw new InventoryServiceException("Empty response from inventory service");
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Product not found in inventory: {}", productId);
            throw new InventoryServiceException("Product not found in inventory: " + productId);
        } catch (ResourceAccessException e) {
            log.error("Inventory service is unavailable: {}", e.getMessage());
            throw new InventoryServiceException("Inventory service is unavailable", e);
        } catch (Exception e) {
            log.error("Error communicating with inventory service: {}", e.getMessage());
            throw new InventoryServiceException("Error communicating with inventory service", e);
        }
    }
}
