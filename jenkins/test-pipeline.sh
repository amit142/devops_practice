#!/bin/bash

# Enhanced Test Script for Jenkins Pipeline
# Supports different test environments and detailed reporting

set -e  # Exit on any error

# Configuration
NAMESPACE=${NAMESPACE:-"ecommerce"}
TEST_TIMEOUT=${TEST_TIMEOUT:-300}
REPORT_FILE="test-results.xml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Logging functions
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run test and track results
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    log_info "Running: $test_name"
    
    if eval "$test_command"; then
        log_success "✓ $test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        log_error "✗ $test_name"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Wait for services to be ready
wait_for_services() {
    log_info "Waiting for services to be ready..."
    
    local services=("user-service" "product-service" "order-service" "web-ui")
    
    for service in "${services[@]}"; do
        log_info "Waiting for $service..."
        kubectl wait --for=condition=available deployment/$service -n $NAMESPACE --timeout=${TEST_TIMEOUT}s
        
        # Additional health check
        local retries=30
        while [ $retries -gt 0 ]; do
            if kubectl get pods -n $NAMESPACE -l app=$service | grep -q "Running"; then
                log_success "$service is ready"
                break
            fi
            sleep 2
            retries=$((retries - 1))
        done
        
        if [ $retries -eq 0 ]; then
            log_error "$service failed to become ready"
            return 1
        fi
    done
}

# Test service health endpoints
test_health_endpoints() {
    log_info "Testing health endpoints..."
    
    # Port forward services for testing
    kubectl port-forward svc/user-service 3001:3001 -n $NAMESPACE &
    kubectl port-forward svc/product-service 3002:3002 -n $NAMESPACE &
    kubectl port-forward svc/order-service 3003:3003 -n $NAMESPACE &
    kubectl port-forward svc/web-ui 8000:8000 -n $NAMESPACE &
    
    sleep 10  # Wait for port forwards to establish
    
    run_test "User Service Health" "curl -f -s http://localhost:3001/health"
    run_test "Product Service Health" "curl -f -s http://localhost:3002/health"
    run_test "Order Service Health" "curl -f -s http://localhost:3003/orders/health"
    run_test "Web UI Health" "curl -f -s http://localhost:8000/health"
}

# Test API endpoints
test_api_endpoints() {
    log_info "Testing API endpoints..."
    
    # User Service Tests
    run_test "Get Users" "curl -f -s http://localhost:3001/users | jq -e '.success == true'"
    run_test "User Registration" "curl -f -s -X POST -H 'Content-Type: application/json' -d '{\"username\":\"testuser\",\"email\":\"test@example.com\",\"firstName\":\"Test\",\"lastName\":\"User\"}' http://localhost:3001/register | jq -e '.success == true'"
    
    # Product Service Tests
    run_test "Get Products" "curl -f -s http://localhost:3002/products | jq -e '.success == true'"
    run_test "Get Categories" "curl -f -s http://localhost:3002/categories | jq -e '.success == true'"
    
    # Order Service Tests
    run_test "Get Orders" "curl -f -s http://localhost:3003/orders | jq -e '.success == true'"
    run_test "Create Order" "curl -f -s -X POST -H 'Content-Type: application/json' -d '{\"userId\":\"1\",\"items\":[{\"productId\":\"1\",\"quantity\":2}]}' http://localhost:3003/orders | jq -e '.success == true'"
}

# Test service-to-service communication
test_service_communication() {
    log_info "Testing service-to-service communication..."
    
    # Test from within cluster
    kubectl run test-pod --image=curlimages/curl:latest --rm -i --restart=Never -n $NAMESPACE -- sh -c "
        curl -f -s http://user-service:3001/health &&
        curl -f -s http://product-service:3002/health &&
        curl -f -s http://order-service:3003/orders/health
    " && run_test "Internal Service Communication" "true" || run_test "Internal Service Communication" "false"
}

# Load testing (basic)
test_load() {
    log_info "Running basic load tests..."
    
    run_test "Load Test - User Service" "ab -n 100 -c 10 http://localhost:3001/health"
    run_test "Load Test - Product Service" "ab -n 100 -c 10 http://localhost:3002/health"
}

# Generate test report
generate_report() {
    log_info "Generating test report..."
    
    cat > $REPORT_FILE << EOF
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="Microservices Integration Tests" tests="$TOTAL_TESTS" failures="$FAILED_TESTS" time="$(date)">
EOF

    if [ $FAILED_TESTS -eq 0 ]; then
        echo "  <testcase name=\"All Tests\" status=\"PASSED\"/>" >> $REPORT_FILE
    else
        echo "  <testcase name=\"Some Tests\" status=\"FAILED\">" >> $REPORT_FILE
        echo "    <failure>$FAILED_TESTS out of $TOTAL_TESTS tests failed</failure>" >> $REPORT_FILE
        echo "  </testcase>" >> $REPORT_FILE
    fi
    
    echo "</testsuite>" >> $REPORT_FILE
    
    log_info "Test report generated: $REPORT_FILE"
}

# Cleanup function
cleanup() {
    log_info "Cleaning up..."
    pkill -f "kubectl port-forward" || true
    kubectl delete pod test-pod -n $NAMESPACE --ignore-not-found=true
}

# Main execution
main() {
    log_info "Starting microservices integration tests..."
    log_info "Namespace: $NAMESPACE"
    log_info "Timeout: $TEST_TIMEOUT seconds"
    
    # Set trap for cleanup
    trap cleanup EXIT
    
    # Run test phases
    wait_for_services
    test_health_endpoints
    test_api_endpoints
    test_service_communication
    
    # Optional load testing (only if requested)
    if [ "$RUN_LOAD_TESTS" = "true" ]; then
        test_load
    fi
    
    # Generate report
    generate_report
    
    # Summary
    echo
    log_info "=== TEST SUMMARY ==="
    log_info "Total Tests: $TOTAL_TESTS"
    log_success "Passed: $PASSED_TESTS"
    if [ $FAILED_TESTS -gt 0 ]; then
        log_error "Failed: $FAILED_TESTS"
    fi
    
    # Exit with appropriate code
    if [ $FAILED_TESTS -eq 0 ]; then
        log_success "All tests passed! 🎉"
        exit 0
    else
        log_error "Some tests failed! ❌"
        exit 1
    fi
}

# Check dependencies
command -v kubectl >/dev/null 2>&1 || { log_error "kubectl is required but not installed"; exit 1; }
command -v curl >/dev/null 2>&1 || { log_error "curl is required but not installed"; exit 1; }
command -v jq >/dev/null 2>&1 || { log_error "jq is required but not installed"; exit 1; }

# Run main function
main "$@"