# Customer Service Extraction Design

**Date:** 2026-07-30  
**Status:** Approved  
**Author:** Claude Code

## Overview

Extract customer management functionality from the existing `java-order-service` into a new, independent `java-customer-service` microservice. The Order Service will communicate with the Customer Service via REST API calls for customer validation.

## Motivation

Split the monolithic order service into two focused microservices:
- **Customer Service** - manages customer CRUD operations
- **Order Service** - manages order lifecycle and orchestration

This provides clearer service boundaries, independent deployment, and follows microservices best practices demonstrated in the existing inventory service integration.

## Architecture

### Service Structure

```
order-management-system/
├── java-customer-service/     (NEW - Port 8081)
│   └── Customer management with H2 database
├── java-order-service/         (MODIFIED - Port 8080)
│   └── Order management, calls Customer & Inventory services
└── python-inventory-service/   (UNCHANGED - Port 8000)
    └── Inventory management
```

### Service Communication

```
Client Request (Create Order)
    ↓
Order Service (8080)
    ├─→ Customer Service (8081) - GET /customers/{id} to validate
    └─→ Inventory Service (8000) - POST /inventory/reserve
```

**Communication Pattern:** Synchronous REST API calls using Spring `RestClient` (consistent with existing `InventoryClient` implementation)

## Components

### New Customer Service (Port 8081)

**Moved from Order Service:**
- `com.customerservice.controller.CustomerController` - REST endpoints for customer operations
- `com.customerservice.service.CustomerService` - business logic
- `com.customerservice.repository.CustomerRepository` - JPA data access
- `com.customerservice.entity.Customer` - JPA entity
- `com.customerservice.dto.CreateCustomerRequest`
- `com.customerservice.dto.UpdateCustomerRequest`
- `com.customerservice.dto.CustomerResponse`
- `com.customerservice.exception.CustomerNotFoundException`
- `com.customerservice.exception.DuplicateCustomerException`
- `com.customerservice.exception.GlobalExceptionHandler` (adapted)

**New components:**
- `CustomerServiceApplication` - Spring Boot main class
- `pom.xml` - Maven configuration (based on order service)
- `application.yml` - configuration with port 8081
- `RestClientConfig` - REST client configuration
- `OpenApiConfig` - Swagger/OpenAPI documentation

**Database:**
- Separate H2 in-memory database: `jdbc:h2:mem:customerdb`
- Schema: `customers` table (id, first_name, last_name, email, phone, created_at)

**API Endpoints:**
- `POST /customers` - Create customer
- `GET /customers` - List all customers
- `GET /customers/{id}` - Get customer by ID (used by Order Service)
- `PUT /customers/{id}` - Update customer
- `DELETE /customers/{id}` - Delete customer
- `GET /health` - Health check

### Modified Order Service (Port 8080)

**Components removed:**
- `CustomerController`
- `CustomerService`
- `CustomerRepository`
- `Customer` entity
- Customer DTOs (CreateCustomerRequest, UpdateCustomerRequest, CustomerResponse)
- Customer exceptions (moved to client exceptions)

**Components added:**
- `com.orderservice.client.CustomerClient` - REST client for Customer Service
  - Method: `CustomerResponse getCustomer(Long customerId)`
  - Throws: `CustomerNotFoundException` on 404
  - Throws: `CustomerServiceException` on connection errors

**Components modified:**
- `OrderService.validateCustomer()` - replace `customerRepository.existsById()` with `customerClient.getCustomer()`
- Exception handling - handle REST client exceptions

**Configuration changes in `application.yml`:**
```yaml
customer:
  service:
    url: http://localhost:8081
```

## Data Flow

### Order Creation Flow (Modified)

1. Client sends `POST /orders` with `customerId` to Order Service
2. Order Service validates customer:
   - Calls `customerClient.getCustomer(customerId)`
   - REST call: `GET http://localhost:8081/customers/{customerId}`
   - If 404 response → throw `CustomerNotFoundException`
   - If 2xx response → customer is valid, proceed
   - If timeout/connection error → throw `CustomerServiceException`
3. Order Service creates draft order (DRAFT status)
4. Order Service reserves inventory via Inventory Service (existing flow)
5. Order Service updates order status (CONFIRMED/FAILED)
6. Return order response to client

### Customer Operations Flow (New Service)

```
Client → Customer Service (8081)
- POST /customers → validate, save, return 201 Created
- GET /customers/{id} → fetch, return 200 OK or 404 Not Found
- PUT /customers/{id} → validate, update, return 200 OK
- DELETE /customers/{id} → delete, return 204 No Content
```

## Error Handling

### Customer Service

Standardized JSON error responses:
```json
{
  "timestamp": "2026-07-30T12:00:00",
  "status": 404,
  "message": "Customer not found with id: 123",
  "path": "/customers/123"
}
```

**HTTP Status Codes:**
- `200 OK` - successful GET/PUT
- `201 Created` - successful POST
- `204 No Content` - successful DELETE
- `400 Bad Request` - validation errors
- `404 Not Found` - customer not found
- `409 Conflict` - duplicate email
- `500 Internal Server Error` - unexpected errors

### Order Service (Customer Client)

**Network error handling:**
- Connection refused → `CustomerServiceException` with message "Customer service unavailable"
- Timeout (5 seconds) → `CustomerServiceException` with message "Customer service timeout"
- 404 from Customer Service → `CustomerNotFoundException` with customer ID
- Other HTTP errors → `CustomerServiceException` with status code

**Order creation error responses:**
- Customer not found → 400 Bad Request
- Customer service unavailable → 503 Service Unavailable
- Other order errors → existing error handling (unchanged)

## Testing Strategy

### Customer Service Tests

**Unit Tests (moved from Order Service):**
- `CustomerServiceTest` (~277 LOC)
  - Test create customer with valid data
  - Test duplicate email detection
  - Test update customer
  - Test delete customer
  - Test customer not found scenarios
  - Mock `CustomerRepository`

**Integration Tests (moved from Order Service):**
- `CustomerControllerIntegrationTest` (~321 LOC)
  - Test all REST endpoints end-to-end
  - Test validation errors
  - Test H2 database integration
  - Use `@SpringBootTest` with `webEnvironment = RANDOM_PORT`

### Order Service Tests (Modified)

**Unit Tests:**
- `OrderServiceTest` - modify to mock `CustomerClient`
  - Mock `customerClient.getCustomer()` to return valid customer
  - Mock `customerClient.getCustomer()` to throw `CustomerNotFoundException`
  - Mock `customerClient.getCustomer()` to throw `CustomerServiceException`
  - Verify order creation fails gracefully on customer errors

**Integration Tests:**
- `OrderControllerIntegrationTest` - use `@MockBean` for `CustomerClient`
  - Mock customer validation responses
  - Test order creation with mocked customer service

### End-to-End Testing

**Manual testing:**
1. Start all three services (Customer 8081, Order 8080, Inventory 8000)
2. Create customer via Customer Service
3. Create order via Order Service with valid customer ID
4. Verify order created successfully
5. Test with invalid customer ID, verify 400 error

**Test script updates:**
- Update `scripts/e2e_test_order_creation.sh` to start Customer Service
- Add customer creation step before order creation

## Configuration

### Customer Service `application.yml`

```yaml
server:
  port: 8081

spring:
  application:
    name: customer-service
  datasource:
    url: jdbc:h2:mem:customerdb
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    database-platform: org.hibernate.dialect.H2Dialect

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Order Service `application.yml` (Addition)

```yaml
customer:
  service:
    url: http://localhost:8081
    timeout: 5000  # 5 seconds
```

**Environment variable override:**
```bash
export CUSTOMER_SERVICE_URL=http://customer-service:8081
```

## Implementation Plan Preview

The implementation will be executed in phases:

1. **Phase 1: Create Customer Service skeleton**
   - Scaffold new Spring Boot project
   - Set up Maven configuration, dependencies
   - Create application structure and configuration

2. **Phase 2: Move customer components**
   - Copy customer entities, DTOs, repositories
   - Copy CustomerController and CustomerService
   - Copy customer exceptions and exception handler
   - Update package names to `com.customerservice`

3. **Phase 3: Add CustomerClient to Order Service**
   - Create CustomerClient using RestClient
   - Configure customer service URL
   - Add exception handling

4. **Phase 4: Modify Order Service**
   - Update OrderService to use CustomerClient
   - Remove customer-related components
   - Update error handling

5. **Phase 5: Move and update tests**
   - Move customer tests to Customer Service
   - Update Order Service tests to mock CustomerClient
   - Verify all tests pass

6. **Phase 6: Update documentation**
   - Update main README with three-service architecture
   - Update service-specific READMEs
   - Update quick start guide

## Non-Goals

- Event-driven communication (out of scope for sample project)
- Distributed transactions (order and customer remain eventually consistent)
- Customer data caching in Order Service
- Shared authentication/authorization (each service independent)
- Production concerns (circuit breakers, retry logic, service mesh)

## Success Criteria

- Customer Service runs independently on port 8081
- All customer operations work via Customer Service API
- Order Service successfully validates customers via REST API
- Order creation works end-to-end across all three services
- All existing tests pass (with modifications for new architecture)
- Documentation updated to reflect three-service architecture
- Sample curl commands work for complete order flow

## Risks and Mitigations

**Risk:** Order creation fails if Customer Service is down  
**Mitigation:** Clear error messages, 503 status code, acceptable for sample project

**Risk:** Network latency slows down order creation  
**Mitigation:** Acceptable for demo, document in README, can add caching later if needed

**Risk:** Breaking existing functionality during extraction  
**Mitigation:** Comprehensive test coverage, phase-by-phase implementation with verification

**Risk:** Configuration complexity with three services  
**Mitigation:** Clear README with startup instructions, environment variable examples
