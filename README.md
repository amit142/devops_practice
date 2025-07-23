# E-Commerce Microservices Platform

A complete DevOps practice project featuring 3 microservices built with different technologies.

## Architecture

- **User Service** (Node.js/Express) - Port 3001
- **Product Service** (Python/Flask) - Port 3002  
- **Order Service** (Java/Spring Boot) - Port 3003

## Quick Start

### 1. Run Services Locally

```bash
# User Service
cd user-service
npm install
npm start

# Product Service  
cd product-service
pip install -r requirements.txt
python app.py

# Order Service
cd order-service
mvn spring-boot:run
```

### 2. Test Services

```bash
chmod +x test-services.sh
./test-services.sh
```

### 3. Build Docker Images

```bash
# User Service
cd user-service
docker build -t user-service:latest .

# Product Service
cd product-service  
docker build -t product-service:latest .

# Order Service
cd order-service
docker build -t order-service:latest .
```

### 4. Deploy to Kubernetes

```bash
kubectl apply -f k8s/
```

## API Endpoints

### User Service (Port 3001)
- `GET /health` - Health check
- `GET /users` - Get all users
- `GET /users/:id` - Get user by ID
- `POST /register` - Register new user
- `POST /auth/login` - User login

### Product Service (Port 3002)
- `GET /health` - Health check
- `GET /products` - Get all products
- `GET /products/:id` - Get product by ID
- `GET /categories` - Get categories
- `GET /products/:id/stock` - Check stock
- `POST /products/:id/reserve` - Reserve stock

### Order Service (Port 3003)
- `GET /orders/health` - Health check
- `GET /orders` - Get all orders
- `GET /orders/:id` - Get order by ID
- `GET /orders/user/:userId` - Get orders by user
- `POST /orders` - Create new order
- `PUT /orders/:id/status` - Update order status

## Example API Calls

```bash
# Register a user
curl -X POST http://localhost:3001/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","firstName":"John","lastName":"Doe"}'

# Get products
curl http://localhost:3002/products

# Create an order
curl -X POST http://localhost:3003/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","items":[{"productId":"1","quantity":2}]}'
```

## DevOps Pipeline

The project includes:
- Docker containers for all services
- Kubernetes deployment manifests
- Jenkins pipeline configuration
- Basic Helm chart structure
- Automated testing script

## Next Steps

1. Set up OpenShift cluster
2. Configure Jenkins with OpenShift integration
3. Implement Helm chart templates
4. Add monitoring and logging
5. Set up CI/CD pipeline