package com.orderservice.client;

import com.orderservice.dto.CustomerResponse;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.CustomerServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with the Customer Service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerClient {

    private final RestTemplate restTemplate;

    @Qualifier("customerServiceUrl")
    private final String customerServiceUrl;

    /**
     * Get customer details from the customer service.
     *
     * @param customerId customer ID
     * @return customer response
     * @throws CustomerNotFoundException if customer is not found
     * @throws CustomerServiceException if service is unavailable or error occurs
     */
    public CustomerResponse getCustomer(Long customerId) {
        String url = customerServiceUrl + "/customers/" + customerId;

        log.info("Calling customer service to validate customer {}", customerId);

        try {
            ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(
                    url,
                    CustomerResponse.class
            );

            CustomerResponse customer = response.getBody();

            if (customer != null) {
                log.info("Customer {} validated successfully", customerId);
                return customer;
            } else {
                throw new CustomerServiceException("Customer service returned null response");
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Customer not found: {}", customerId);
            throw new CustomerNotFoundException(customerId);
        } catch (ResourceAccessException e) {
            log.error("Customer service is unavailable: {}", e.getMessage());
            throw new CustomerServiceException("Customer service is unavailable", e);
        } catch (Exception e) {
            log.error("Error communicating with customer service: {}", e.getMessage());
            throw new CustomerServiceException("Error communicating with customer service", e);
        }
    }
}
