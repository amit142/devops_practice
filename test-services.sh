#!/bin/bash

echo "=== E-Commerce Microservices Test Script ==="
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Service URLs
USER_SERVICE="http://localhost:3001"
PRODUCT_SERVICE="http://localhost:3002"
ORDER_SERVICE="http://localhost:3003"

# Function to test service health
test_health() {
    local service_name=$1
    local url=$2
    
    echo -n "Testing $service_name health... "
    if curl -s "$url/health" > /dev/null; then
        echo -e "${GREEN}✓ Healthy${NC}"
        return 0
    else
        echo -e "${RED}✗ Unhealthy${NC}"
        return 1
    fi
}

# Function to test endpoint
test_endpoint() {
    local description=$1
    local url=$2
    local method=${3:-GET}
    local data=$4
    
    echo -n "Testing $description... "
    
    if [ "$method" = "POST" ]; then
        response=$(curl -s -X POST -H "Content-Type: application/json" -d "$data" "$url")
    else
        response=$(curl -s "$url")
    fi
    
    if [ $? -eq 0 ] && echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✓ Success${NC}"
        return 0
    else
        echo -e "${RED}✗ Failed${NC}"
        echo "Response: $response"
        return 1
    fi
}

echo "1. Testing Service Health Checks"
echo "================================"
test_health "User Service" "$USER_SERVICE"
test_health "Product Service" "$PRODUCT_SERVICE"
test_health "Order Service" "$ORDER_SERVICE"
echo

echo "2. Testing User Service"
echo "======================"
test_endpoint "Get all users" "$USER_SERVICE/users"
test_endpoint "Get user by ID" "$USER_SERVICE/users/1"
test_endpoint "User registration" "$USER_SERVICE/register" "POST" '{"username":"testuser","email":"test@example.com","firstName":"Test","lastName":"User"}'
test_endpoint "User login" "$USER_SERVICE/auth/login" "POST" '{"username":"testuser"}'
echo

echo "3. Testing Product Service"
echo "========================="
test_endpoint "Get all products" "$PRODUCT_SERVICE/products"
test_endpoint "Get product by ID" "$PRODUCT_SERVICE/products/1"
test_endpoint "Get categories" "$PRODUCT_SERVICE/categories"
test_endpoint "Check product stock" "$PRODUCT_SERVICE/products/1/stock"
echo

echo "4. Testing Order Service"
echo "======================="
test_endpoint "Get all orders" "$ORDER_SERVICE/orders"
test_endpoint "Create order" "$ORDER_SERVICE/orders" "POST" '{"userId":"1","items":[{"productId":"1","quantity":2}]}'
echo

echo "=== Test Complete ==="