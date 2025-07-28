// This pipeline mimics a freestyle project but uses Kubernetes agents
pipeline {
    agent {
        kubernetes {
            label 'freestyle-test'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins: slave
    type: freestyle-test
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    resources:
      requests:
        memory: "128Mi"
        cpu: "100m"
      limits:
        memory: "256Mi"
        cpu: "200m"
  - name: tools
    image: busybox:latest
    command: ['sleep']
    args: ['99d']
    resources:
      requests:
        memory: "64Mi"
        cpu: "50m"
      limits:
        memory: "128Mi"
        cpu: "100m"
"""
        }
    }
    
    stages {
        stage('Freestyle-like Build') {
            steps {
                // This mimics what a freestyle project would do
                container('tools') {
                    sh '''
                        echo "🚀 Freestyle-style Project - Dynamic Agent Test"
                        echo "=============================================="
                        
                        echo "📋 Build Information:"
                        echo "  Job Name: $JOB_NAME"
                        echo "  Build Number: $BUILD_NUMBER"
                        echo "  Node Name: $NODE_NAME"
                        echo "  Workspace: $WORKSPACE"
                        
                        echo ""
                        echo "🖥️  System Information:"
                        echo "  Hostname: $(hostname)"
                        echo "  Current User: $(whoami)"
                        echo "  Current Directory: $(pwd)"
                        
                        echo ""
                        echo "💾 Resource Information:"
                        echo "  Memory Info:"
                        cat /proc/meminfo | head -3
                        
                        echo ""
                        echo "⏱️  Simulating Freestyle Work:"
                        for i in 1 2 3; do
                            echo "  Step $i/3: Processing..."
                            sleep 2
                            echo "    - Current time: $(date)"
                            echo "    - Working in: $(pwd)"
                        done
                        
                        echo ""
                        echo "📁 Creating some artifacts:"
                        echo "Build completed at $(date)" > build-result.txt
                        echo "Node: $(hostname)" >> build-result.txt
                        echo "Workspace: $(pwd)" >> build-result.txt
                        
                        echo ""
                        echo "✅ Freestyle-style test completed!"
                        echo "=============================================="
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Archive artifacts like a freestyle project would
            archiveArtifacts artifacts: '*.txt', allowEmptyArchive: true
        }
        success {
            echo "✅ Freestyle-style build successful!"
        }
    }
}