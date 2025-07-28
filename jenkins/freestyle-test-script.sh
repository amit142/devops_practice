#!/bin/bash

echo "🚀 Freestyle Project - Dynamic Agent Test"
echo "=========================================="

echo "📋 Build Information:"
echo "  Job Name: $JOB_NAME"
echo "  Build Number: $BUILD_NUMBER"
echo "  Node Name: $NODE_NAME"
echo "  Workspace: $WORKSPACE"
echo "  Jenkins URL: $JENKINS_URL"

echo ""
echo "🖥️  System Information:"
echo "  Hostname: $(hostname)"
echo "  IP Address: $(hostname -i 2>/dev/null || echo 'N/A')"
echo "  Operating System: $(uname -a)"
echo "  Current User: $(whoami)"
echo "  Current Directory: $(pwd)"

echo ""
echo "💾 Resource Information:"
echo "  Memory Info:"
cat /proc/meminfo | head -3
echo "  CPU Info:"
cat /proc/cpuinfo | grep "model name" | head -1 || echo "  CPU info not available"
echo "  Disk Usage:"
df -h / 2>/dev/null || echo "  Disk info not available"

echo ""
echo "🌐 Network Information:"
echo "  Network Interfaces:"
ip addr show 2>/dev/null | grep -E "(inet|eth|lo)" || echo "  Network info not available"

echo ""
echo "☸️  Kubernetes Information:"
echo "  Namespace: ${POD_NAMESPACE:-jenkins}"
echo "  Pod Name: ${HOSTNAME}"
echo "  Service Account: $(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace 2>/dev/null || echo 'N/A')"

echo ""
echo "🔧 Available Tools:"
which docker >/dev/null 2>&1 && echo "  ✅ Docker: $(docker --version 2>/dev/null)" || echo "  ❌ Docker: Not available"
which kubectl >/dev/null 2>&1 && echo "  ✅ kubectl: $(kubectl version --client --short 2>/dev/null)" || echo "  ❌ kubectl: Not available"
which git >/dev/null 2>&1 && echo "  ✅ Git: $(git --version 2>/dev/null)" || echo "  ❌ Git: Not available"
which java >/dev/null 2>&1 && echo "  ✅ Java: $(java -version 2>&1 | head -1)" || echo "  ❌ Java: Not available"

echo ""
echo "📁 Workspace Contents:"
ls -la $WORKSPACE 2>/dev/null || echo "  Workspace is empty or not accessible"

echo ""
echo "🔍 Environment Variables (Jenkins related):"
env | grep -E "(JENKINS|BUILD|JOB)" | sort

echo ""
echo "⏱️  Test Duration Simulation:"
for i in {1..5}; do
    echo "  Step $i/5: Processing..."
    sleep 2
    echo "    - Current time: $(date)"
    echo "    - Random number: $RANDOM"
done

echo ""
echo "✅ Freestyle project test completed successfully!"
echo "🕐 Test finished at: $(date)"
echo "=========================================="