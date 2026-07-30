# Java Order Service

Spring Boot microservice for customer and order management with inventory service integration.

## Overview

Manages customer data and order lifecycle, coordinating with Python Inventory Service for stock reservation and release.

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
java -jar target/java-order-service-1.0.0.jar
```

Access: http://localhost:8080/swagger-ui.html

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customers` | Create customer |
| GET | `/customers` | List customers |
| GET | `/customers/{id}` | Get customer |
| PUT | `/customers/{id}` | Update customer |
| DELETE | `/customers/{id}` | Delete customer |
| POST | `/orders` | Create order |
| GET | `/orders` | List orders |
| GET | `/orders/{id}` | Get order |
| GET | `/orders/customer/{customerId}` | Get customer orders |
| POST | `/orders/{id}/cancel` | Cancel order |
| GET | `/health` | Health check |

## Project Structure

```
java-order-service/
├── src/main/java/com/orderservice/
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic
│   ├── repository/       # Data access
│   ├── entity/           # JPA entities
│   ├── dto/              # Request/response objects
│   ├── exception/        # Custom exceptions
│   ├── config/           # Configuration
│   └── client/           # External service clients
├── src/test/java/        # Tests
└── pom.xml
```

## Configuration

**Application** (`application.yml`):
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:orderdb
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console

inventory:
  service:
    url: http://localhost:8000
```

**Environment Variables:**
```bash
export SERVER_PORT=8081
export INVENTORY_SERVICE_URL=http://inventory:8000
./mvnw spring-boot:run
```

## Database Schema

**Customers:** id, first_name, last_name, email (unique), phone, created_at

**Orders:** id, customer_id, status (DRAFT/CONFIRMED/CANCELLED/FAILED), total_amount, created_at, updated_at

**Order Items:** id, order_id, product_id, quantity, price, subtotal

## Order Lifecycle

### Creation Flow
1. Validate customer exists
2. Create order in DRAFT status
3. Reserve stock for each item (call Inventory Service)
4. Update to CONFIRMED (success) or FAILED (insufficient stock)

### Cancellation Flow
1. Validate order is CONFIRMED
2. Release stock for each item (call Inventory Service)
3. Update to CANCELLED status

## Testing

```bash
# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest=*ServiceTest

# Integration tests only
./mvnw test -Dtest=*IntegrationTest

# With coverage
./mvnw test jacoco:report
open target/site/jacoco/index.html
```

**Coverage:**
- Unit tests: 629 LOC (Customer: 277, Order: 352)
- Integration tests: 650 LOC (Customer: 321, Order: 329)

## H2 Console

Access in-memory database:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (empty)

## Error Handling

Standardized JSON responses:
```json
{
  "timestamp": "2026-07-21T12:00:00",
  "status": 404,
  "message": "Customer not found with id: 999",
  "path": "/customers/999"
}
```

**Status Codes:** 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found), 409 (Conflict), 500 (Server Error), 503 (Service Unavailable)

## Logging

Configuration in `logback-spring.xml`:
- Console: Color-coded for development
- Files: `logs/order-service.log` (rolling, 30 days)
- Errors: `logs/order-service-error.log` (rolling, 90 days)

## Features

### Customer Management
- CRUD operations
- Email uniqueness validation
- Phone format validation
- Error handling

### Order Management
- Multi-item orders
- Automatic inventory reservation
- Status tracking (DRAFT → CONFIRMED/FAILED)
- Cancellation with stock release
- Transactional processing

## Development

### Code Style
- Follow Java naming conventions
- Use Lombok for boilerplate reduction
- Keep methods focused (<20 lines)
- Add JavaDoc for public APIs

### Adding Features
1. Implement entity/repository
2. Create service with business logic
3. Add controller endpoints
4. Write unit tests (mock dependencies)
5. Write integration tests (@SpringBootTest)
6. Update OpenAPI documentation

## Troubleshooting

**Port 8080 in use:**
```bash
lsof -i :8080
kill -9 <PID>
# Or: ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Cannot connect to Inventory Service:**
- Verify Python service running: `curl http://localhost:8000/health`
- Check `inventory.service.url` in application.yml
- Orders will be marked FAILED if service unavailable

**H2 Console not accessible:**
- Verify `spring.h2.console.enabled=true`
- Access at http://localhost:8080/h2-console
- Use JDBC URL: `jdbc:h2:mem:orderdb`

## Production Considerations

- Replace H2 with PostgreSQL/MySQL
- Add authentication/authorization
- Implement rate limiting
- Add circuit breakers (Resilience4j)
- Set up monitoring (Actuator, Prometheus)
- Enable distributed tracing (Zipkin, Jaeger)
- Use external configuration (Spring Cloud Config)

## License

Part of Order Management System demonstration project.
