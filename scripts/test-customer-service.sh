#!/bin/bash

echo "Testing Customer Service..."

BASE_URL="http://localhost:8081"

# Test health
echo "1. Testing health endpoint..."
curl -s $BASE_URL/health | jq '.'

# Create customer
echo ""
echo "2. Creating customer..."
CUSTOMER_RESPONSE=$(curl -s -X POST $BASE_URL/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@example.com",
    "phone": "+1-555-111-2222"
  }')

echo $CUSTOMER_RESPONSE | jq '.'
CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')

# Get customer by ID
echo ""
echo "3. Getting customer by ID: $CUSTOMER_ID..."
curl -s $BASE_URL/customers/$CUSTOMER_ID | jq '.'

# Get all customers
echo ""
echo "4. Getting all customers..."
curl -s $BASE_URL/customers | jq '.'

# Update customer
echo ""
echo "5. Updating customer..."
curl -s -X PUT $BASE_URL/customers/$CUSTOMER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson-Smith",
    "email": "alice.johnson-smith@example.com",
    "phone": "+1-555-111-3333"
  }' | jq '.'

# Delete customer
echo ""
echo "6. Deleting customer..."
curl -s -X DELETE $BASE_URL/customers/$CUSTOMER_ID -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "Customer Service tests completed!"
