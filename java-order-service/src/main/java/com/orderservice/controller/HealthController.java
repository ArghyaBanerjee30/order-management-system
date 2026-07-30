package com.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for service monitoring.
 */
@RestController
@Slf4j
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * Basic health check endpoint.
     *
     * @return Health status
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "order-service");
        return ResponseEntity.ok(health);
    }

    /**
     * Readiness check endpoint (includes database connectivity).
     *
     * @return Readiness status
     */
    @GetMapping("/health/ready")
    @Operation(summary = "Readiness check", description = "Returns service readiness status including database connectivity")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("service", "order-service");

        // Check database connectivity
        boolean databaseUp = checkDatabase();
        readiness.put("database", databaseUp ? "UP" : "DOWN");
        readiness.put("ready", databaseUp);

        if (databaseUp) {
            log.debug("Readiness check: Service is ready");
            return ResponseEntity.ok(readiness);
        } else {
            log.error("Readiness check: Service is not ready - database is down");
            return ResponseEntity.status(503).body(readiness);
        }
    }

    /**
     * Liveness check endpoint (basic service liveness).
     *
     * @return Liveness status
     */
    @GetMapping("/health/live")
    @Operation(summary = "Liveness check", description = "Returns service liveness status")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> liveness = new HashMap<>();
        liveness.put("status", "UP");
        liveness.put("service", "order-service");
        log.debug("Liveness check: Service is alive");
        return ResponseEntity.ok(liveness);
    }

    /**
     * Check database connectivity.
     *
     * @return true if database is accessible, false otherwise
     */
    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }
}
