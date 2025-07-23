#!/bin/bash

echo "🧹 E-Commerce Microservices K8s Cleanup Script"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Confirm deletion
read -p "Are you sure you want to delete all E-Commerce microservices? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_info "Cleanup cancelled."
    exit 0
fi

print_info "Cleaning up E-Commerce microservices..."

# Delete all resources in the ecommerce namespace
print_info "Deleting all resources in ecommerce namespace..."
kubectl delete all --all -n ecommerce

# Delete ingress
print_info "Deleting ingress..."
kubectl delete ingress ecommerce-ingress -n ecommerce 2>/dev/null || true

# Delete configmap
print_info "Deleting configmap..."
kubectl delete configmap service-config -n ecommerce 2>/dev/null || true

# Delete namespace (this will delete everything in it)
print_info "Deleting ecommerce namespace..."
kubectl delete namespace ecommerce

# Clean up Docker images (optional)
read -p "Do you want to remove Docker images as well? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Removing Docker images..."
    docker rmi user-service:latest 2>/dev/null || true
    docker rmi product-service:latest 2>/dev/null || true
    docker rmi order-service:latest 2>/dev/null || true
    docker rmi web-ui:latest 2>/dev/null || true
    print_status "Docker images removed"
fi

print_status "Cleanup complete! 🧹"