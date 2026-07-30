#!/bin/bash

echo "Starting all services for Order Management System..."

# Start Customer Service
echo "Starting Customer Service on port 8081..."
cd java-customer-service
./mvnw spring-boot:run > ../logs/customer-service.log 2>&1 &
CUSTOMER_PID=$!
cd ..

sleep 5

# Start Order Service
echo "Starting Order Service on port 8080..."
cd java-order-service
./mvnw spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
cd ..

sleep 5

# Start Inventory Service
echo "Starting Inventory Service on port 8000..."
cd python-inventory-service
source venv/bin/activate
uvicorn main:app --reload > ../logs/inventory-service.log 2>&1 &
INVENTORY_PID=$!
cd ..

sleep 5

echo "All services started!"
echo "Customer Service PID: $CUSTOMER_PID (port 8081)"
echo "Order Service PID: $ORDER_PID (port 8080)"
echo "Inventory Service PID: $INVENTORY_PID (port 8000)"

echo ""
echo "To stop all services:"
echo "kill $CUSTOMER_PID $ORDER_PID $INVENTORY_PID"
