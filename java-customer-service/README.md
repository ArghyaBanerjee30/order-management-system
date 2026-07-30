# Java Customer Service

Spring Boot microservice for customer management.

## Overview

Manages customer CRUD operations with validation and error handling. Provides REST API for customer data access.

## Technology Stack

- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory), Lombok
- SpringDoc OpenAPI 3 (Swagger), JUnit 5, Mockito

## Quick Start

```bash
# Build and run
./mvnw spring-boot:run

# Production build
./mvnw clean package -DskipTests
java -jar target/java-customer-service-1.0.0.jar
```

Access: http://localhost:8081/swagger-ui.html

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customers` | Create customer |
| GET | `/customers` | List customers |
| GET | `/customers/{id}` | Get customer |
| PUT | `/customers/{id}` | Update customer |
| DELETE | `/customers/{id}` | Delete customer |
| GET | `/health` | Health check |

## Configuration

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:customerdb
    username: sa
    password:
```

## Testing

```bash
# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest=*ServiceTest

# Integration tests only
./mvnw test -Dtest=*IntegrationTest
```

## License

Part of Order Management System demonstration project.
