# Python Inventory Service

FastAPI-based microservice for product catalog and inventory management.

## Overview

Handles product management, inventory tracking, stock reservations, and history logging for the Order Management System.

## Technology Stack

- Python 3.14.5, FastAPI 0.109.0, SQLAlchemy 2.0.25
- Pydantic 2.5.3 (validation), Alembic 1.13.1 (migrations)
- SQLite Database, pytest (testing)

## Quick Start

```bash
# Setup
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
alembic upgrade head

# Run (development)
uvicorn main:app --reload

# Run (production)
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
```

Access: http://localhost:8000/docs

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/products` | Create product |
| GET | `/products` | List products (pagination supported) |
| GET | `/products/{id}` | Get product |
| PUT | `/products/{id}` | Update product |
| DELETE | `/products/{id}` | Soft delete product |
| GET | `/inventory/{product_id}` | Get inventory |
| POST | `/inventory/reserve` | Reserve stock |
| POST | `/inventory/release` | Release stock |
| POST | `/inventory/add` | Add stock |
| POST | `/inventory/remove` | Remove stock |
| GET | `/health` | Health check |

## Project Structure

```
python-inventory-service/
├── database/          # DB configuration
├── models/           # SQLAlchemy models
├── repository/       # Data access layer
├── routers/          # API endpoints
├── schemas/          # Pydantic schemas
├── services/         # Business logic
├── tests/            # Unit & integration tests
├── alembic/          # Database migrations
├── main.py           # Application entry
└── requirements.txt
```

## Configuration

**Database** (`database/config.py`):
```python
# SQLite (development)
SQLALCHEMY_DATABASE_URL = "sqlite:///./inventory.db"

# PostgreSQL (production)
SQLALCHEMY_DATABASE_URL = "postgresql://user:password@localhost/dbname"
```

**Logging** (`logging_config.py`):
- Console output (development)
- File logs: `logs/inventory-service.log`
- Error logs: `logs/inventory-service-error.log`
- Auto-rotation: Daily with cleanup

## Database Schema

**Products:** id, name, description, price, sku (unique), active

**Inventory:** product_id (PK), available_quantity, reserved_quantity, last_updated

**Inventory History:** id, product_id, action (ADD/REMOVE/RESERVE/RELEASE), quantity, timestamp

## Testing

```bash
# All tests
pytest -v

# With coverage
pytest --cov=. --cov-report=html

# Specific tests
pytest tests/test_product_service.py -v
pytest tests/test_inventory_router.py -v
```

**Coverage:**
- Unit tests: 556 LOC (product: 248, inventory: 308)
- Integration tests: 637 LOC (product: 276, inventory: 361)

## Features

### Product Management
- CRUD operations with validation
- SKU uniqueness (auto-uppercase)
- Soft delete (active/inactive flag)
- Pagination support

### Inventory Management
- Atomic stock operations (thread-safe)
- Available vs reserved quantity tracking
- Automatic history logging
- Real-time availability checks

## Error Handling

Standardized JSON responses:
```json
{
  "timestamp": "2026-07-21T12:00:00",
  "status": 404,
  "message": "Product not found with id: 999",
  "path": "/products/999"
}
```

**Status Codes:** 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found), 409 (Conflict), 422 (Validation Error), 500 (Server Error)

## Development

### Database Migrations

```bash
# Create migration
alembic revision --autogenerate -m "Description"

# Apply migrations
alembic upgrade head

# Rollback
alembic downgrade -1
```

### Code Style
- Follow PEP 8
- Use type hints
- Write docstrings
- Keep functions focused

## Troubleshooting

**Port 8000 in use:**
```bash
lsof -i :8000
kill -9 <PID>
# Or use different port: uvicorn main:app --port 8001
```

**Module not found:**
```bash
source venv/bin/activate
pip install -r requirements.txt
```

**Database errors:**
```bash
rm inventory.db
alembic upgrade head
```

## Production Considerations

- Replace SQLite with PostgreSQL
- Add authentication/authorization
- Implement rate limiting
- Enable response compression
- Use connection pooling
- Add caching (Redis)
- Set up monitoring

## License

Part of Order Management System demonstration project.
