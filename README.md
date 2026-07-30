# Order Management System

A microservices-based order management system built with Java Spring Boot and Python FastAPI, demonstrating modern distributed architecture patterns.

## Overview

Distributed application managing customer orders and inventory across two independent microservices:

- **Java Order Service** (Port 8080) - Customer and order management
- **Python Inventory Service** (Port 8000) - Product catalog and inventory management

## Technology Stack

### Java Order Service
- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory)
- SpringDoc OpenAPI, JUnit 5, Mockito

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

**Python Inventory Service:**
```bash
cd python-inventory-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
alembic upgrade head
uvicorn main:app --reload
```
Verify: http://localhost:8000/docs

**Java Order Service:**
```bash
cd java-order-service
./mvnw spring-boot:run
```
Verify: http://localhost:8080/swagger-ui.html

### Quick Test

```bash
# Create customer
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"+1-555-123-4567"}'

# Create product
curl -X POST http://localhost:8000/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","description":"Test","price":99.99,"sku":"TEST-001"}'

# Add inventory
curl -X POST http://localhost:8000/inventory/add \
  -H "Content-Type: application/json" \
  -d '{"product_id":1,"quantity":100}'

# Create order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"orderItems":[{"productId":1,"quantity":2,"price":99.99}]}'
```

## Architecture

### Order Creation Flow
```
Client -> Order Service: POST /orders
Order Service: Validate customer, create DRAFT order
Order Service -> Inventory Service: POST /inventory/reserve
Inventory Service: Reserve stock (atomic)
Order Service: Update to CONFIRMED status
Order Service -> Client: Order confirmation
```

### Order Cancellation Flow
```
Client -> Order Service: POST /orders/{id}/cancel
Order Service: Validate order is CONFIRMED
Order Service -> Inventory Service: POST /inventory/release
Inventory Service: Release stock
Order Service: Update to CANCELLED status
Order Service -> Client: Cancellation confirmation
```

## API Endpoints

### Order Service (8080)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/customers` | POST/GET | Manage customers |
| `/customers/{id}` | GET/PUT/DELETE | Customer operations |
| `/orders` | POST/GET | Manage orders |
| `/orders/{id}` | GET | Get order details |
| `/orders/{id}/cancel` | POST | Cancel order |

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
# Java tests
cd java-order-service && ./mvnw test

# Python tests
cd python-inventory-service && pytest -v

# E2E tests (requires both services running)
./scripts/e2e_test_order_creation.sh
```

## Configuration

**Java Order Service** (`application.yml`):
```yaml
server:
  port: 8080
inventory:
  service:
    url: http://localhost:8000
```

**Python Inventory Service** (`database/config.py`):
```python
SQLALCHEMY_DATABASE_URL = "sqlite:///./inventory.db"
```

## Documentation

- Interactive API Docs: http://localhost:8080/swagger-ui.html (Java), http://localhost:8000/docs (Python)
- [Java Service README](java-order-service/README.md)
- [Python Service README](python-inventory-service/README.md)
- [Sample Data](data/README.md)

## License

Demonstration project showcasing microservices architecture.
