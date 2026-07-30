#!/bin/bash

# Script to load sample data into both services
# Usage: ./load-sample-data.sh

set -e

echo "========================================="
echo "Loading Sample Data"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Service URLs
ORDER_SERVICE="http://localhost:8080"
INVENTORY_SERVICE="http://localhost:8000"

# Check if services are running
echo -e "\n${BLUE}Checking if services are running...${NC}"

if ! curl -sf "${ORDER_SERVICE}/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Order Service is not running on ${ORDER_SERVICE}${NC}"
    echo "Please start the Order Service first"
    exit 1
fi
echo -e "${GREEN}✓ Order Service is running${NC}"

if ! curl -sf "${INVENTORY_SERVICE}/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Inventory Service is not running on ${INVENTORY_SERVICE}${NC}"
    echo "Please start the Inventory Service first"
    exit 1
fi
echo -e "${GREEN}✓ Inventory Service is running${NC}"

# Load customers via API
echo -e "\n${BLUE}Loading sample customers...${NC}"

CUSTOMERS=(
    '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","phone":"+1-555-123-4567"}'
    '{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com","phone":"+1-555-234-5678"}'
    '{"firstName":"Michael","lastName":"Johnson","email":"michael.j@example.com","phone":"+1-555-345-6789"}'
    '{"firstName":"Emily","lastName":"Williams","email":"emily.w@example.com","phone":"+1-555-456-7890"}'
    '{"firstName":"David","lastName":"Brown","email":"david.brown@example.com","phone":"+1-555-567-8901"}'
)

CUSTOMER_COUNT=0
for customer in "${CUSTOMERS[@]}"; do
    if curl -s -X POST "${ORDER_SERVICE}/customers" \
        -H "Content-Type: application/json" \
        -d "$customer" > /dev/null; then
        CUSTOMER_COUNT=$((CUSTOMER_COUNT + 1))
    fi
done
echo -e "${GREEN}✓ Loaded ${CUSTOMER_COUNT} customers${NC}"

# Load products via API
echo -e "\n${BLUE}Loading sample products...${NC}"

PRODUCTS=(
    '{"name":"Laptop Pro 15","description":"High-performance laptop","price":1299.99,"sku":"LAPTOP-PRO-15"}'
    '{"name":"Wireless Mouse","description":"Ergonomic wireless mouse","price":29.99,"sku":"MOUSE-WIRELESS-01"}'
    '{"name":"Mechanical Keyboard","description":"RGB mechanical keyboard","price":89.99,"sku":"KEYBOARD-MECH-RGB"}'
    '{"name":"USB-C Hub","description":"7-in-1 USB-C hub","price":49.99,"sku":"HUB-USBC-7IN1"}'
    '{"name":"Webcam HD 1080p","description":"Full HD webcam","price":79.99,"sku":"WEBCAM-HD-1080"}'
    '{"name":"Monitor 27\"","description":"27-inch 4K monitor","price":399.99,"sku":"MONITOR-27-4K"}'
    '{"name":"Headphones Bluetooth","description":"Noise-canceling Bluetooth headphones","price":149.99,"sku":"HEADPHONES-BT-NC"}'
    '{"name":"External SSD 1TB","description":"Portable external SSD 1TB","price":119.99,"sku":"SSD-EXT-1TB"}'
)

PRODUCT_COUNT=0
for product in "${PRODUCTS[@]}"; do
    if curl -s -X POST "${INVENTORY_SERVICE}/products" \
        -H "Content-Type: application/json" \
        -d "$product" > /dev/null; then
        PRODUCT_COUNT=$((PRODUCT_COUNT + 1))
    fi
done
echo -e "${GREEN}✓ Loaded ${PRODUCT_COUNT} products${NC}"

# Add inventory for products
echo -e "\n${BLUE}Adding inventory for products...${NC}"

INVENTORY_COUNT=0
for i in {1..8}; do
    QUANTITY=$((50 + (i * 20)))
    if curl -s -X POST "${INVENTORY_SERVICE}/inventory/add" \
        -H "Content-Type: application/json" \
        -d "{\"product_id\":${i},\"quantity\":${QUANTITY}}" > /dev/null; then
        INVENTORY_COUNT=$((INVENTORY_COUNT + 1))
    fi
done
echo -e "${GREEN}✓ Added inventory for ${INVENTORY_COUNT} products${NC}"

# Verify data
echo -e "\n${BLUE}Verifying data...${NC}"

CUSTOMERS_LOADED=$(curl -s "${ORDER_SERVICE}/customers" | grep -o '"id"' | wc -l | tr -d ' ')
echo -e "  Customers in database: ${CUSTOMERS_LOADED}"

PRODUCTS_LOADED=$(curl -s "${INVENTORY_SERVICE}/products" | grep -o '"id"' | wc -l | tr -d ' ')
echo -e "  Products in database: ${PRODUCTS_LOADED}"

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Sample Data Loaded Successfully!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "You can now:"
echo "  - View customers: ${ORDER_SERVICE}/swagger-ui.html"
echo "  - View products: ${INVENTORY_SERVICE}/docs"
echo "  - Create test orders using the API"
echo ""

exit 0
