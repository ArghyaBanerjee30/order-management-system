# Customer Service

Manages customer profiles and information.

## Stack

Java 17 • Spring Boot 3.2 • PostgreSQL • Maven

## Prerequisites

- JDK 17+
- PostgreSQL database

## Configuration

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/customerdb
spring.datasource.username=postgres
spring.datasource.password=password
```

## Start Service

```bash
./mvnw spring-boot:run
```

Runs on port **8081** | API docs at `/swagger-ui.html`

## API Endpoints

- `POST /customers` - Create customer
- `GET /customers` - List all customers
- `GET /customers/{id}` - Get customer details
- `PUT /customers/{id}` - Update customer
- `DELETE /customers/{id}` - Delete customer

## Run Tests

```bash
./mvnw test
```
