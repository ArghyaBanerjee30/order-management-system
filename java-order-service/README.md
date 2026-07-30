# Order Service

Manages order processing and coordinates with Customer and Inventory services.

## Stack

Java 17 • Spring Boot 3.2 • PostgreSQL • Maven

## Prerequisites

- JDK 17+
- PostgreSQL database
- Customer Service (port 8081)
- Inventory Service (port 8000)

## Configuration

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/orderdb
spring.datasource.username=postgres
spring.datasource.password=password
customer.service.url=http://localhost:8081
inventory.service.url=http://localhost:8000
```

## Start Service

```bash
./mvnw spring-boot:run
```

Runs on port **8080** | API docs at `/swagger-ui.html`

## API Endpoints

- `POST /orders` - Create order (reserves inventory)
- `GET /orders` - List all orders
- `GET /orders/{id}` - Get order details
- `GET /orders/customer/{customerId}` - Get customer orders
- `POST /orders/{id}/cancel` - Cancel order (releases inventory)

## Order Flow

1. Validate customer exists
2. Create order in DRAFT status
3. Reserve inventory for items
4. Update to CONFIRMED (success) or FAILED (insufficient stock)

## Run Tests

```bash
./mvnw test
```
