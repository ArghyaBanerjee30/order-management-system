# Order Management System

Microservices-based system for managing customers, orders, and inventory.

## System Architecture

Three independent services communicate via REST APIs:

```
┌─────────────────┐
│  Order Service  │ :8080
│   (Java/Spring) │
└────────┬────────┘
         │
         ├──→ Customer Service :8081 (Java/Spring)
         │
         └──→ Inventory Service :8000 (Python/FastAPI)
```

## Services

| Service | Port | Technology | Purpose |
|---------|------|------------|---------|
| **Order** | 8080 | Java 17 • Spring Boot | Order processing and orchestration |
| **Customer** | 8081 | Java 17 • Spring Boot | Customer profile management |
| **Inventory** | 8000 | Python 3.11+ • FastAPI | Product catalog and stock management |

## Prerequisites

- JDK 17+
- Python 3.11+
- PostgreSQL (production) or H2/SQLite (development)

## Quick Start

**1. Start Inventory Service**
```bash
cd python-inventory-service
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
alembic upgrade head
uvicorn inventory_service.main:app --reload --port 8000
```

**2. Start Customer Service**
```bash
cd java-customer-service
./mvnw spring-boot:run
```

**3. Start Order Service**
```bash
cd java-order-service
./mvnw spring-boot:run
```

## Verification

- Inventory API: http://localhost:8000/docs
- Customer API: http://localhost:8081/swagger-ui.html
- Order API: http://localhost:8080/swagger-ui.html

## Order Flow

**Creating an Order**
1. Validate customer exists (Customer Service)
2. Create order in DRAFT status
3. Reserve inventory for each item (Inventory Service)
4. Update order to CONFIRMED or FAILED

**Cancelling an Order**
1. Validate order is CONFIRMED
2. Release reserved inventory (Inventory Service)
3. Update order to CANCELLED

## Key Endpoints

**Create Order**
```bash
POST http://localhost:8080/orders
{
  "customerId": 1,
  "orderItems": [
    {"productId": 1, "quantity": 2, "price": 99.99}
  ]
}
```

**Reserve Inventory**
```bash
POST http://localhost:8000/inventory/reserve
{
  "product_id": 1,
  "quantity": 2
}
```

## Testing

```bash
# Customer Service
cd java-customer-service && ./mvnw test

# Order Service
cd java-order-service && ./mvnw test

# Inventory Service
cd python-inventory-service && pytest
```

## Configuration

Each service has its own README with detailed configuration:
- [Customer Service](java-customer-service/README.md)
- [Order Service](java-order-service/README.md)
- [Inventory Service](python-inventory-service/README.md)

## Technology Stack

**Backend Frameworks**
- Spring Boot 3.2 (Java services)
- FastAPI (Python service)

**Data Layer**
- Spring Data JPA + Hibernate
- SQLAlchemy
- PostgreSQL / H2 / SQLite

**API Documentation**
- SpringDoc OpenAPI (Swagger)
- FastAPI automatic OpenAPI docs

**Testing**
- JUnit 5 + Mockito (Java)
- pytest + httpx (Python)
