# 🚀 Kubernetes Deployment Guide

Complete guide to deploy the E-Commerce Microservices platform on Kubernetes.

## 📋 Prerequisites

### Required Tools
- **Docker** - For building container images
- **kubectl** - Kubernetes command-line tool
- **Kubernetes cluster** - One of the following:
  - Minikube (local development)
  - Kind (Kubernetes in Docker)
  - Docker Desktop with Kubernetes
  - Cloud provider (GKE, EKS, AKS)
  - On-premise cluster

### Verify Prerequisites
```bash
# Check Docker
docker --version

# Check kubectl
kubectl version --client

# Check cluster connection
kubectl cluster-info
```

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                       │
├─────────────────────────────────────────────────────────────┤
│  Namespace: ecommerce                                       │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ User Service│  │Product Svc  │  │Order Service│        │
│  │ (Node.js)   │  │ (Python)    │  │ (Java)      │        │
│  │ Port: 3001  │  │ Port: 3002  │  │ Port: 3003  │        │
│  │ Replicas: 2 │  │ Replicas: 2 │  │ Replicas: 2 │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Web UI (Python HTTP)                  │   │
│  │              Port: 8000, Replicas: 2               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   Ingress                           │   │
│  │            (nginx-ingress-controller)               │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Deployment

### Option 1: Automated Deployment (Recommended)
```bash
# Make script executable
chmod +x deploy-k8s.sh

# Run deployment script
./deploy-k8s.sh
```

### Option 2: Manual Step-by-Step Deployment

#### Step 1: Build Docker Images
```bash
# Build all service images
docker build -t user-service:latest ./user-service/
docker build -t product-service:latest ./product-service/
docker build -t order-service:latest ./order-service/
docker build -t web-ui:latest ./web-ui/
```

#### Step 2: Create Namespace
```bash
kubectl apply -f k8s/namespace.yaml
```

#### Step 3: Deploy Services
```bash
# Deploy all services to the ecommerce namespace
kubectl apply -f k8s/user-service.yaml -n ecommerce
kubectl apply -f k8s/product-service.yaml -n ecommerce
kubectl apply -f k8s/order-service.yaml -n ecommerce
kubectl apply -f k8s/web-ui.yaml -n ecommerce
```

#### Step 4: Deploy Ingress (Optional)
```bash
# Only if you have an ingress controller installed
kubectl apply -f k8s/ingress.yaml -n ecommerce
```

#### Step 5: Verify Deployment
```bash
# Check pod status
kubectl get pods -n ecommerce

# Check services
kubectl get services -n ecommerce

# Check deployments
kubectl get deployments -n ecommerce
```

## 🌐 Accessing the Application

### Method 1: Port Forwarding (Easiest)
```bash
# Forward Web UI port
kubectl port-forward svc/web-ui 8000:8000 -n ecommerce

# Access at: http://localhost:8000
```

### Method 2: NodePort (Minikube/Kind)
```bash
# Web UI is exposed on NodePort 30800
# Access at: http://localhost:30800
# Or: http://<node-ip>:30800
```

### Method 3: LoadBalancer (Cloud Providers)
```bash
# Get external IP
kubectl get svc web-ui -n ecommerce

# Access using the EXTERNAL-IP shown
```

### Method 4: Ingress (Advanced)
```bash
# Add to /etc/hosts
echo "127.0.0.1 ecommerce.local" >> /etc/hosts

# Access at: http://ecommerce.local
```

## 📊 Monitoring and Management

### Check Application Status
```bash
# View all resources
kubectl get all -n ecommerce

# Check pod logs
kubectl logs -f deployment/user-service -n ecommerce
kubectl logs -f deployment/product-service -n ecommerce
kubectl logs -f deployment/order-service -n ecommerce
kubectl logs -f deployment/web-ui -n ecommerce

# Describe problematic pods
kubectl describe pod <pod-name> -n ecommerce
```

### Scaling Services
```bash
# Scale user service to 3 replicas
kubectl scale deployment user-service --replicas=3 -n ecommerce

# Scale all services
kubectl scale deployment user-service --replicas=3 -n ecommerce
kubectl scale deployment product-service --replicas=3 -n ecommerce
kubectl scale deployment order-service --replicas=3 -n ecommerce
kubectl scale deployment web-ui --replicas=3 -n ecommerce
```

### Health Checks
```bash
# Test service health through port-forward
kubectl port-forward svc/user-service 3001:3001 -n ecommerce &
curl http://localhost:3001/health

kubectl port-forward svc/product-service 3002:3002 -n ecommerce &
curl http://localhost:3002/health

kubectl port-forward svc/order-service 3003:3003 -n ecommerce &
curl http://localhost:3003/orders/health
```

## 🔧 Configuration

### Environment Variables
Services are configured with these environment variables:

**Order Service:**
- `SERVICES_USER_SERVICE_URL`: http://user-service:3001
- `SERVICES_PRODUCT_SERVICE_URL`: http://product-service:3002

**All Services:**
- Health checks configured with appropriate paths
- Resource limits can be added to deployment manifests

### Service Discovery
Services communicate using Kubernetes DNS:
- `user-service.ecommerce.svc.cluster.local:3001`
- `product-service.ecommerce.svc.cluster.local:3002`
- `order-service.ecommerce.svc.cluster.local:3003`

## 🛠️ Troubleshooting

### Common Issues

#### 1. ImagePullBackOff Error
```bash
# Check if images exist locally
docker images | grep -E "(user-service|product-service|order-service|web-ui)"

# If using remote registry, push images first
docker tag user-service:latest your-registry/user-service:latest
docker push your-registry/user-service:latest
```

#### 2. Service Not Ready
```bash
# Check pod logs
kubectl logs -f deployment/user-service -n ecommerce

# Check pod description
kubectl describe pod <pod-name> -n ecommerce

# Check service endpoints
kubectl get endpoints -n ecommerce
```

#### 3. Cannot Access Application
```bash
# Check service type and ports
kubectl get svc -n ecommerce

# Verify port-forward is working
kubectl port-forward svc/web-ui 8000:8000 -n ecommerce

# Check ingress status (if using)
kubectl get ingress -n ecommerce
kubectl describe ingress ecommerce-ingress -n ecommerce
```

#### 4. Inter-service Communication Issues
```bash
# Test service-to-service connectivity
kubectl exec -it deployment/order-service -n ecommerce -- curl http://user-service:3001/health
kubectl exec -it deployment/order-service -n ecommerce -- curl http://product-service:3002/health
```

### Debug Commands
```bash
# Get detailed pod information
kubectl get pods -o wide -n ecommerce

# Check resource usage
kubectl top pods -n ecommerce

# View events
kubectl get events -n ecommerce --sort-by='.lastTimestamp'

# Shell into a pod
kubectl exec -it <pod-name> -n ecommerce -- /bin/sh
```

## 🧹 Cleanup

### Quick Cleanup
```bash
# Run cleanup script
./cleanup-k8s.sh
```

### Manual Cleanup
```bash
# Delete all resources in namespace
kubectl delete all --all -n ecommerce

# Delete namespace
kubectl delete namespace ecommerce

# Remove Docker images (optional)
docker rmi user-service:latest product-service:latest order-service:latest web-ui:latest
```

## 🔒 Production Considerations

### Security
- [ ] Use non-root containers (already implemented)
- [ ] Add resource limits and requests
- [ ] Implement network policies
- [ ] Use secrets for sensitive data
- [ ] Enable RBAC

### Monitoring
- [ ] Add Prometheus metrics
- [ ] Configure Grafana dashboards
- [ ] Set up alerting rules
- [ ] Implement distributed tracing

### High Availability
- [ ] Use multiple replicas (already configured)
- [ ] Configure pod disruption budgets
- [ ] Set up horizontal pod autoscaling
- [ ] Use anti-affinity rules

### Storage
- [ ] Add persistent volumes for data
- [ ] Configure database backends
- [ ] Implement backup strategies

## 📚 Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)

## 🎯 Next Steps

1. **Deploy to your cluster** using the provided scripts
2. **Access the Web UI** and test all functionality
3. **Monitor the services** using kubectl commands
4. **Scale services** based on load requirements
5. **Implement production features** as needed

Happy deploying! 🚀