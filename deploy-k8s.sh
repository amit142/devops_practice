#!/bin/bash

echo "🚀 E-Commerce Microservices K8s Deployment Script"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

print_info "Starting deployment process..."

# Step 1: Build Docker images
echo
print_info "Step 1: Building Docker images..."

print_info "Building User Service image..."
docker build -t user-service:latest ./user-service/
if [ $? -eq 0 ]; then
    print_status "User Service image built successfully"
else
    print_error "Failed to build User Service image"
    exit 1
fi

print_info "Building Product Service image..."
docker build -t product-service:latest ./product-service/
if [ $? -eq 0 ]; then
    print_status "Product Service image built successfully"
else
    print_error "Failed to build Product Service image"
    exit 1
fi

print_info "Building Order Service image..."
docker build -t order-service:latest ./order-service/
if [ $? -eq 0 ]; then
    print_status "Order Service image built successfully"
else
    print_error "Failed to build Order Service image"
    exit 1
fi

print_info "Building Web UI image..."
docker build -t web-ui:latest ./web-ui/
if [ $? -eq 0 ]; then
    print_status "Web UI image built successfully"
else
    print_error "Failed to build Web UI image"
    exit 1
fi

# Step 2: Create namespace
echo
print_info "Step 2: Creating Kubernetes namespace..."
kubectl apply -f k8s/namespace.yaml
if [ $? -eq 0 ]; then
    print_status "Namespace created/updated successfully"
else
    print_error "Failed to create namespace"
    exit 1
fi

# Step 3: Deploy services
echo
print_info "Step 3: Deploying microservices..."

print_info "Deploying User Service..."
kubectl apply -f k8s/user-service.yaml -n ecommerce
if [ $? -eq 0 ]; then
    print_status "User Service deployed successfully"
else
    print_error "Failed to deploy User Service"
fi

print_info "Deploying Product Service..."
kubectl apply -f k8s/product-service.yaml -n ecommerce
if [ $? -eq 0 ]; then
    print_status "Product Service deployed successfully"
else
    print_error "Failed to deploy Product Service"
fi

print_info "Deploying Order Service..."
kubectl apply -f k8s/order-service.yaml -n ecommerce
if [ $? -eq 0 ]; then
    print_status "Order Service deployed successfully"
else
    print_error "Failed to deploy Order Service"
fi

print_info "Deploying Web UI..."
kubectl apply -f k8s/web-ui.yaml -n ecommerce
if [ $? -eq 0 ]; then
    print_status "Web UI deployed successfully"
else
    print_error "Failed to deploy Web UI"
fi

# Step 4: Deploy ingress (optional)
echo
print_info "Step 4: Deploying Ingress (optional)..."
kubectl apply -f k8s/ingress.yaml -n ecommerce
if [ $? -eq 0 ]; then
    print_status "Ingress deployed successfully"
else
    print_warning "Ingress deployment failed (this is optional if you don't have ingress controller)"
fi

# Step 5: Wait for deployments
echo
print_info "Step 5: Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/user-service -n ecommerce
kubectl wait --for=condition=available --timeout=300s deployment/product-service -n ecommerce
kubectl wait --for=condition=available --timeout=300s deployment/order-service -n ecommerce
kubectl wait --for=condition=available --timeout=300s deployment/web-ui -n ecommerce

# Step 6: Show deployment status
echo
print_info "Step 6: Deployment Status"
echo "=========================="
kubectl get pods -n ecommerce
echo
kubectl get services -n ecommerce
echo
kubectl get ingress -n ecommerce

# Step 7: Access information
echo
print_info "🎉 Deployment Complete!"
echo "======================="
print_info "Access your application:"
echo
print_info "Via NodePort (if using minikube/kind):"
echo "  Web UI: http://localhost:30800"
echo
print_info "Via Port Forward:"
echo "  kubectl port-forward svc/web-ui 8000:8000 -n ecommerce"
echo "  Then access: http://localhost:8000"
echo
print_info "Via Ingress (if configured):"
echo "  Add '127.0.0.1 ecommerce.local' to /etc/hosts"
echo "  Then access: http://ecommerce.local"
echo
print_info "To check logs:"
echo "  kubectl logs -f deployment/user-service -n ecommerce"
echo "  kubectl logs -f deployment/product-service -n ecommerce"
echo "  kubectl logs -f deployment/order-service -n ecommerce"
echo "  kubectl logs -f deployment/web-ui -n ecommerce"
echo
print_info "To scale services:"
echo "  kubectl scale deployment user-service --replicas=3 -n ecommerce"
echo
print_status "Happy coding! 🚀"