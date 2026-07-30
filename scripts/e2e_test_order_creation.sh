#!/bin/bash

# E2E Test: Complete Order Creation Flow
# Tests customer creation, product creation, inventory setup, and order creation

set -e

echo "========================================="
echo "E2E Test: Order Creation Flow"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service URLs
ORDER_SERVICE="http://localhost:8080"
INVENTORY_SERVICE="http://localhost:8000"

# Test data
CUSTOMER_EMAIL="e2e.test.$(date +%s)@example.com"
PRODUCT_SKU="E2E-SKU-$(date +%s)"

echo -e "${BLUE}Step 1: Check services are running${NC}"
if ! curl -s -f "${ORDER_SERVICE}/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Order Service is not running on ${ORDER_SERVICE}${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Order Service is running${NC}"

if ! curl -s -f "${INVENTORY_SERVICE}/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Inventory Service is not running on ${INVENTORY_SERVICE}${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Inventory Service is running${NC}"

echo ""
echo -e "${BLUE}Step 2: Create Customer${NC}"
CUSTOMER_RESPONSE=$(curl -s -X POST "${ORDER_SERVICE}/customers" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"E2E\",
    \"lastName\": \"Test\",
    \"email\": \"${CUSTOMER_EMAIL}\",
    \"phone\": \"+1-555-999-0001\"
  }")

CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
if [ -z "$CUSTOMER_ID" ]; then
    echo -e "${RED}✗ Failed to create customer${NC}"
    echo "$CUSTOMER_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ Customer created with ID: ${CUSTOMER_ID}${NC}"

echo ""
echo -e "${BLUE}Step 3: Create Product${NC}"
PRODUCT_RESPONSE=$(curl -s -X POST "${INVENTORY_SERVICE}/products" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"E2E Test Product\",
    \"description\": \"Product for E2E testing\",
    \"price\": 99.99,
    \"sku\": \"${PRODUCT_SKU}\"
  }")

PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
if [ -z "$PRODUCT_ID" ]; then
    echo -e "${RED}✗ Failed to create product${NC}"
    echo "$PRODUCT_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ Product created with ID: ${PRODUCT_ID}${NC}"

echo ""
echo -e "${BLUE}Step 4: Add Inventory${NC}"
INVENTORY_RESPONSE=$(curl -s -X POST "${INVENTORY_SERVICE}/inventory/add" \
  -H "Content-Type: application/json" \
  -d "{
    \"product_id\": ${PRODUCT_ID},
    \"quantity\": 100
  }")

AVAILABLE_QTY=$(echo "$INVENTORY_RESPONSE" | grep -o '"available_quantity":[0-9]*' | grep -o '[0-9]*')
if [ "$AVAILABLE_QTY" != "100" ]; then
    echo -e "${RED}✗ Failed to add inventory${NC}"
    echo "$INVENTORY_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ Inventory added: 100 units available${NC}"

echo ""
echo -e "${BLUE}Step 5: Create Order${NC}"
ORDER_RESPONSE=$(curl -s -X POST "${ORDER_SERVICE}/orders" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": ${CUSTOMER_ID},
    \"orderItems\": [
      {
        \"productId\": ${PRODUCT_ID},
        \"quantity\": 10,
        \"price\": 99.99
      }
    ]
  }")

ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
ORDER_STATUS=$(echo "$ORDER_RESPONSE" | grep -o '"status":"[^"]*"' | grep -o ':[^:]*$' | tr -d ':"')

if [ -z "$ORDER_ID" ]; then
    echo -e "${RED}✗ Failed to create order${NC}"
    echo "$ORDER_RESPONSE"
    exit 1
fi

if [ "$ORDER_STATUS" != "CONFIRMED" ]; then
    echo -e "${RED}✗ Order status is ${ORDER_STATUS}, expected CONFIRMED${NC}"
    echo "$ORDER_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ Order created with ID: ${ORDER_ID}, Status: ${ORDER_STATUS}${NC}"

echo ""
echo -e "${BLUE}Step 6: Verify Inventory Reservation${NC}"
INVENTORY_CHECK=$(curl -s "${INVENTORY_SERVICE}/inventory/${PRODUCT_ID}")

FINAL_AVAILABLE=$(echo "$INVENTORY_CHECK" | grep -o '"available_quantity":[0-9]*' | grep -o '[0-9]*')
FINAL_RESERVED=$(echo "$INVENTORY_CHECK" | grep -o '"reserved_quantity":[0-9]*' | grep -o '[0-9]*')

if [ "$FINAL_AVAILABLE" != "90" ] || [ "$FINAL_RESERVED" != "10" ]; then
    echo -e "${RED}✗ Inventory state incorrect${NC}"
    echo "Expected: available=90, reserved=10"
    echo "Actual: available=${FINAL_AVAILABLE}, reserved=${FINAL_RESERVED}"
    echo "$INVENTORY_CHECK"
    exit 1
fi
echo -e "${GREEN}✓ Inventory correctly updated: available=${FINAL_AVAILABLE}, reserved=${FINAL_RESERVED}${NC}"

echo ""
echo "========================================="
echo -e "${GREEN}✓ E2E Test PASSED${NC}"
echo "========================================="
echo "Summary:"
echo "  - Customer ID: ${CUSTOMER_ID}"
echo "  - Product ID: ${PRODUCT_ID}"
echo "  - Order ID: ${ORDER_ID}"
echo "  - Order Status: ${ORDER_STATUS}"
echo "  - Inventory: ${FINAL_AVAILABLE} available, ${FINAL_RESERVED} reserved"
echo "========================================="

exit 0
