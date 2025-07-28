# Jenkins Setup Guide for Microservices CI/CD

## Prerequisites

### 1. Jenkins Installation
```bash
# Using Docker (recommended for testing)
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 2. Required Jenkins Plugins
Install these plugins via Jenkins UI (Manage Jenkins > Manage Plugins):

**Essential Plugins:**
- Pipeline
- Docker Pipeline
- Kubernetes CLI
- Git
- Blue Ocean (for better UI)
- Slack Notification (optional)
- Credentials Binding

**Security Plugins:**
- OWASP Markup Formatter
- Build Timeout

## Jenkins Configuration

### 1. Global Tool Configuration
Go to **Manage Jenkins > Global Tool Configuration**:

**Maven:**
- Name: `Maven-3.9`
- Install automatically: ✅
- Version: `3.9.0`

**Node.js:**
- Name: `NodeJS-18`
- Install automatically: ✅
- Version: `18.x`

**Docker:**
- Name: `Docker`
- Install automatically: ✅

### 2. Credentials Setup
Go to **Manage Jenkins > Manage Credentials > System > Global credentials**:

**Docker Registry Credentials:**
- Kind: `Username with password`
- ID: `docker-registry-creds`
- Username: `your-registry-username`
- Password: `your-registry-password`

**Docker Registry URL:**
- Kind: `Secret text`
- ID: `docker-registry-url`
- Secret: `your-registry.com` (without https://)

**Kubernetes Config:**
- Kind: `Secret file`
- ID: `kubeconfig`
- File: Upload your `~/.kube/config` file

**Slack Token (optional):**
- Kind: `Secret text`
- ID: `slack-token`
- Secret: `your-slack-bot-token`

### 3. Pipeline Job Creation

**Option A: Multibranch Pipeline (Recommended)**
1. New Item > Multibranch Pipeline
2. Branch Sources > Git
3. Repository URL: `your-git-repo-url`
4. Credentials: Select your Git credentials
5. Build Configuration: `by Jenkinsfile`
6. Script Path: `jenkins/Jenkinsfile`

**Option B: Regular Pipeline**
1. New Item > Pipeline
2. Pipeline Definition: `Pipeline script from SCM`
3. SCM: Git
4. Repository URL: `your-git-repo-url`
5. Script Path: `jenkins/Jenkinsfile`

## Environment-Specific Configurations

### Development Environment
Create a separate pipeline job with these parameters:
- `NAMESPACE`: `ecommerce-dev`
- `DOCKER_REGISTRY`: `localhost:5000`
- Deploy on every commit

### Production Environment
- `NAMESPACE`: `ecommerce-prod`
- `DOCKER_REGISTRY`: `your-production-registry.com`
- Deploy only on `main` branch
- Require manual approval

## Pipeline Parameters

Add these build parameters to your job:

**Boolean Parameters:**
- `FORCE_DEPLOY`: Force deployment regardless of branch
- `SKIP_TESTS`: Skip test execution
- `SKIP_SECURITY_SCAN`: Skip security scanning

**Choice Parameters:**
- `DEPLOY_ENVIRONMENT`: `dev`, `staging`, `prod`

## Monitoring and Notifications

### Build Notifications
The pipeline includes Slack notifications. Configure:
1. Install Slack plugin
2. Add Slack workspace integration
3. Update channel names in Jenkinsfile

### Build Metrics
Monitor these key metrics:
- Build success rate
- Build duration
- Deployment frequency
- Mean time to recovery (MTTR)

## Troubleshooting

### Common Issues:

**Docker Permission Denied:**
```bash
# Add jenkins user to docker group
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

**Kubernetes Connection Issues:**
- Verify kubeconfig file is valid
- Check network connectivity to cluster
- Ensure proper RBAC permissions

**Build Timeouts:**
- Increase timeout in pipeline options
- Optimize Docker builds with multi-stage builds
- Use build caching

### Debug Commands:
```bash
# Check Jenkins logs
docker logs jenkins

# Test kubectl connection
kubectl get nodes

# Test Docker registry connection
docker login your-registry.com
```

## Security Best Practices

1. **Use Jenkins Credentials Store** - Never hardcode secrets
2. **Enable CSRF Protection** - Prevent cross-site request forgery
3. **Regular Updates** - Keep Jenkins and plugins updated
4. **Limit Build Permissions** - Use least privilege principle
5. **Audit Logs** - Monitor build activities
6. **Secure Docker Socket** - Consider Docker-in-Docker alternatives

## Performance Optimization

1. **Parallel Builds** - Build services simultaneously
2. **Docker Layer Caching** - Use multi-stage builds
3. **Build Agents** - Distribute builds across multiple nodes
4. **Artifact Caching** - Cache dependencies between builds
5. **Resource Limits** - Set appropriate CPU/memory limits