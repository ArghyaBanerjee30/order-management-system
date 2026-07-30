# Order Management System

A microservices-based order management system built with Java Spring Boot and Python FastAPI, demonstrating modern distributed architecture patterns.

## Overview

Distributed application managing customers, orders, and inventory across three independent microservices:

- **Java Customer Service** (Port 8081) - Customer CRUD operations
- **Java Order Service** (Port 8080) - Order management and orchestration
- **Python Inventory Service** (Port 8000) - Product catalog and inventory management

## Technology Stack

### Java Customer Service
- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory)
- SpringDoc OpenAPI, JUnit 5, Mockito

### Java Order Service
- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory)
- SpringDoc OpenAPI, JUnit 5, Mockito
- RestTemplate for inter-service communication

### Python Inventory Service
- Python 3.14.5, FastAPI 0.109.0, SQLAlchemy 2.0.25
- SQLite Database
- Alembic (migrations), pytest

## Quick Start

### Prerequisites
- Java 17+
- Python 3.12+
- Maven 3.8+ (wrapper included)

### Start Services

**1. Python Inventory Service:**
```bash
cd python-inventory-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
alembic upgrade head
uvicorn main:app --reload
```
Verify: http://localhost:8000/docs

**2. Java Customer Service:**
```bash
cd java-customer-service
./mvnw spring-boot:run
```
Verify: http://localhost:8081/swagger-ui.html

**3. Java Order Service:**
```bash
cd java-order-service
./mvnw spring-boot:run
```
Verify: http://localhost:8080/swagger-ui.html

### Quick Test

```bash
# Create customer
CUSTOMER_RESPONSE=$(curl -X POST http://localhost:8081/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"+1-555-123-4567"}')
CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')

# Create product
PRODUCT_RESPONSE=$(curl -X POST http://localhost:8000/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","description":"Test","price":99.99,"sku":"TEST-001"}')
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')

# Add inventory
curl -X POST http://localhost:8000/inventory/add \
  -H "Content-Type: application/json" \
  -d "{\"product_id\":$PRODUCT_ID,\"quantity\":100}"

# Create order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d "{\"customerId\":$CUSTOMER_ID,\"orderItems\":[{\"productId\":$PRODUCT_ID,\"quantity\":2,\"price\":99.99}]}"
```

## Architecture

### Service Communication

```
Client
  ↓
Order Service (8080)
  ├─→ Customer Service (8081) - Validate customer exists
  └─→ Inventory Service (8000) - Reserve/Release stock
```

### Order Creation Flow
```
1. Client → Order Service: POST /orders
2. Order Service → Customer Service: GET /customers/{id} (validate)
3. Order Service: Create DRAFT order
4. Order Service → Inventory Service: POST /inventory/reserve (atomic)
5. Order Service: Update to CONFIRMED status
6. Order Service → Client: Order confirmation
```

### Order Cancellation Flow
```
1. Client → Order Service: POST /orders/{id}/cancel
2. Order Service: Validate order is CONFIRMED
3. Order Service → Inventory Service: POST /inventory/release
4. Order Service: Update to CANCELLED status
5. Order Service → Client: Cancellation confirmation
```

## API Endpoints

### Customer Service (8081)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/customers` | POST/GET | Manage customers |
| `/customers/{id}` | GET/PUT/DELETE | Customer operations |
| `/health` | GET | Health check |

### Order Service (8080)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/orders` | POST/GET | Manage orders |
| `/orders/{id}` | GET | Get order details |
| `/orders/{id}/cancel` | POST | Cancel order |
| `/orders/customer/{customerId}` | GET | Get customer orders |
| `/health` | GET | Health check |

### Inventory Service (8000)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/products` | POST/GET | Manage products |
| `/products/{id}` | GET/PUT/DELETE | Product operations |
| `/inventory/{product_id}` | GET | Get inventory |
| `/inventory/reserve` | POST | Reserve stock |
| `/inventory/release` | POST | Release stock |
| `/inventory/add` | POST | Add stock |

## Testing

```bash
# Customer Service tests
cd java-customer-service && ./mvnw test

# Order Service tests
cd java-order-service && ./mvnw test

# Inventory Service tests
cd python-inventory-service && pytest -v

# E2E tests (requires all services running)
./scripts/test-order-flow-e2e.sh
```

## Configuration

**Customer Service** (`application.yml`):
```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:h2:mem:customerdb
```

**Order Service** (`application.yml`):
```yaml
server:
  port: 8080
customer:
  service:
    url: http://localhost:8081
inventory:
  service:
    url: http://localhost:8000
```

**Inventory Service** (`database/config.py`):
```python
SQLALCHEMY_DATABASE_URL = "sqlite:///./inventory.db"
```

## Documentation

- Interactive API Docs:
  - Customer Service: http://localhost:8081/swagger-ui.html
  - Order Service: http://localhost:8080/swagger-ui.html
  - Inventory Service: http://localhost:8000/docs
- [Customer Service README](java-customer-service/README.md)
- [Order Service README](java-order-service/README.md)
- [Inventory Service README](python-inventory-service/README.md)

## License

Demonstration project showcasing microservices architecture.
