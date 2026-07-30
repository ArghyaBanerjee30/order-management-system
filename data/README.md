# Sample Data

Sample data scripts for populating the Order Management System with test data.

## Files

- `sample-customers.sql` - 10 sample customers
- `sample-products.sql` - 15 sample products with inventory
- `load-sample-data.sh` - Script to load data via REST API

## Loading Data

### Via REST API (Recommended)

```bash
# Ensure both services are running on ports 8080 and 8000
cd data
./load-sample-data.sh
```

This creates:
- 5 customers via POST /customers
- 8 products via POST /products
- Inventory for all products via POST /inventory/add

### Via H2 Console (Order Service)

1. Access http://localhost:8080/h2-console
2. Connection: `jdbc:h2:mem:orderdb`, username: `sa`, password: (empty)
3. Copy and run `sample-customers.sql`

### Via SQLite (Inventory Service)

```bash
cd python-inventory-service
sqlite3 inventory.db < ../data/sample-products.sql
```

## Sample Data Contents

### Customers
| ID | Name | Email | Phone |
|----|------|-------|-------|
| 1 | John Doe | john.doe@example.com | +1-555-123-4567 |
| 2 | Jane Smith | jane.smith@example.com | +1-555-234-5678 |
| ... | ... | ... | ... |

### Products
| ID | Name | SKU | Price | Stock |
|----|------|-----|-------|-------|
| 1 | Laptop Pro 15 | LAPTOP-PRO-15 | $1,299.99 | 50 |
| 2 | Wireless Mouse | MOUSE-WIRELESS-01 | $29.99 | 200 |
| ... | ... | ... | ... | ... |

## Custom Data

### Add Customer
```bash
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@test.com","phone":"+1-555-0000"}'
```

### Add Product
```bash
curl -X POST http://localhost:8000/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Product","description":"Test","price":99.99,"sku":"TEST-001"}'
```

### Add Inventory
```bash
curl -X POST http://localhost:8000/inventory/add \
  -H "Content-Type: application/json" \
  -d '{"product_id":1,"quantity":100}'
```

### Create Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"orderItems":[{"productId":1,"quantity":1,"price":99.99}]}'
```

## Clearing Data

**Order Service (H2):** Restart service (in-memory database clears automatically)

**Inventory Service (SQLite):**
```bash
cd python-inventory-service
rm inventory.db
alembic upgrade head
```

## Troubleshooting

**Script permission denied:**
```bash
chmod +x load-sample-data.sh
```

**Services not running:**
- Verify Order Service: http://localhost:8080/health
- Verify Inventory Service: http://localhost:8000/health

**Duplicate key errors:**
- Clear existing data first
- Modify emails/SKUs to be unique

## Note

For development and testing only. Do not use in production environments.
