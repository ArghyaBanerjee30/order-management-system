# Inventory Service

Manages product catalog and inventory with stock reservation capabilities.

## Stack

Python 3.11+ • FastAPI • SQLAlchemy • PostgreSQL

## Prerequisites

- Python 3.11+
- PostgreSQL database

## Setup

```bash
pip install -r requirements.txt
```

## Configuration

Update `.env` or `database/config.py`:

```env
DATABASE_URL=postgresql://postgres:password@localhost:5432/inventorydb
```

## Database Setup

```bash
alembic upgrade head
```

## Start Service

```bash
uvicorn inventory_service.main:app --reload --port 8000
```

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
