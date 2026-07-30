# Inventory Service

Manages product catalog and inventory with stock reservation capabilities.

## Stack

Python 3.11+ • FastAPI • SQLAlchemy • PostgreSQL

## Prerequisites

- Python 3.11+
- PostgreSQL database

## Quick Start

**Option 1: Use startup script (Recommended)**
```bash
cd python-inventory-service
./start.sh
```

**Option 2: Manual setup**
```bash
cd python-inventory-service

# Create and setup virtual environment
python3 -m venv venv
./venv/bin/pip install --upgrade pip setuptools wheel
./venv/bin/pip install -r requirements.txt

# Setup database
./venv/bin/alembic upgrade head

# Start service
./venv/bin/uvicorn inventory_service.main:app --reload --port 8000
```

## Configuration

Update `database/config.py` for PostgreSQL:

```python
SQLALCHEMY_DATABASE_URL = "postgresql://postgres:password@localhost:5432/inventorydb"
```

Default: SQLite (`inventory.db`)

Runs on port **8000** | API docs at `/docs`

## API Endpoints

**Products**
- `POST /products` - Create product
- `GET /products` - List products
- `GET /products/{id}` - Get product details
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product

**Inventory**
- `GET /inventory/{product_id}` - Get inventory levels
- `POST /inventory/add` - Add stock
- `POST /inventory/remove` - Remove stock
- `POST /inventory/reserve` - Reserve stock for order
- `POST /inventory/release` - Release reserved stock

## Run Tests

```bash
pytest tests/
```
