package com.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST clients.
 */
@Configuration
public class RestClientConfig {

    @Value("${inventory.service.url:http://localhost:8001}")
    private String inventoryServiceUrl;

    @Value("${rest.client.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${rest.client.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    @Bean
    public String inventoryServiceUrl() {
        return inventoryServiceUrl;
    }
}
