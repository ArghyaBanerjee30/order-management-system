#!/bin/bash

echo "Testing End-to-End Order Flow with Three Services..."

CUSTOMER_URL="http://localhost:8081"
ORDER_URL="http://localhost:8080"
INVENTORY_URL="http://localhost:8000"

# Step 1: Create customer
echo "Step 1: Creating customer..."
CUSTOMER_RESPONSE=$(curl -s -X POST $CUSTOMER_URL/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Bob",
    "lastName": "Williams",
    "email": "bob.williams@example.com",
    "phone": "+1-555-222-3333"
  }')

echo $CUSTOMER_RESPONSE | jq '.'
CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')
echo "Customer ID: $CUSTOMER_ID"

# Step 2: Create product
echo ""
echo "Step 2: Creating product..."
PRODUCT_RESPONSE=$(curl -s -X POST $INVENTORY_URL/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Widget",
    "description": "A test product",
    "price": 49.99,
    "sku": "TEST-WIDGET-001"
  }')

echo $PRODUCT_RESPONSE | jq '.'
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')
echo "Product ID: $PRODUCT_ID"

# Step 3: Add inventory
echo ""
echo "Step 3: Adding inventory..."
curl -s -X POST $INVENTORY_URL/inventory/add \
  -H "Content-Type: application/json" \
  -d "{
    \"product_id\": $PRODUCT_ID,
    \"quantity\": 100
  }" | jq '.'

# Step 4: Create order
echo ""
echo "Step 4: Creating order..."
ORDER_RESPONSE=$(curl -s -X POST $ORDER_URL/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"orderItems\": [
      {
        \"productId\": $PRODUCT_ID,
        \"quantity\": 2,
        \"price\": 49.99
      }
    ]
  }")

echo $ORDER_RESPONSE | jq '.'
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')
ORDER_STATUS=$(echo $ORDER_RESPONSE | jq -r '.status')
echo "Order ID: $ORDER_ID"
echo "Order Status: $ORDER_STATUS"

# Step 5: Verify inventory was reserved
echo ""
echo "Step 5: Checking inventory after order..."
curl -s $INVENTORY_URL/inventory/$PRODUCT_ID | jq '.'

# Step 6: Cancel order
echo ""
echo "Step 6: Cancelling order..."
CANCEL_RESPONSE=$(curl -s -X POST $ORDER_URL/orders/$ORDER_ID/cancel)
echo $CANCEL_RESPONSE | jq '.'
CANCEL_STATUS=$(echo $CANCEL_RESPONSE | jq -r '.status')
echo "Order Status after cancellation: $CANCEL_STATUS"

# Step 7: Verify inventory was released
echo ""
echo "Step 7: Checking inventory after cancellation..."
curl -s $INVENTORY_URL/inventory/$PRODUCT_ID | jq '.'

echo ""
echo "End-to-End Order Flow Test Completed!"
echo "Summary:"
echo "  Customer ID: $CUSTOMER_ID"
echo "  Product ID: $PRODUCT_ID"
echo "  Order ID: $ORDER_ID"
echo "  Final Order Status: $CANCEL_STATUS"
