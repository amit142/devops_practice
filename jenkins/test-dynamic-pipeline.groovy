pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins: slave
    test: dynamic-pipeline
spec:
  serviceAccountName: jenkins
  containers:
  - name: busybox
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
  - name: docker
    image: docker:dind
    securityContext:
      privileged: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
    resources:
      requests:
        memory: "128Mi"
        cpu: "100m"
      limits:
        memory: "256Mi"
        cpu: "200m"
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    
    stages {
        stage('Agent Info') {
            steps {
                script {
                    echo "🚀 DYNAMIC AGENT TEST - This should create a new pod!"
                    echo "=================================================="
                    echo "Build Number: ${BUILD_NUMBER}"
                    echo "Node Name: ${NODE_NAME}"
                    echo "Workspace: ${WORKSPACE}"
                }
                
                container('busybox') {
                    sh '''
                        echo "📦 Running in BusyBox container on dynamic agent"
                        echo "Hostname: $(hostname)"
                        echo "Pod IP: $(hostname -i)"
                        echo "Current user: $(whoami)"
                        echo "Working directory: $(pwd)"
                        
                        echo ""
                        echo "🔍 This pod should appear in your kubectl monitoring!"
                        echo "Pod name: $(hostname)"
                        echo "Namespace: jenkins"
                        
                        echo ""
                        echo "⏱️  Keeping agent alive for 30 seconds so you can see it..."
                        for i in $(seq 1 10); do
                            echo "  Heartbeat $i/10 - $(date)"
                            sleep 3
                        done
                        
                        echo ""
                        echo "✅ Dynamic agent test completed!"
                        echo "This pod will now terminate..."
                    '''
                }
            }
        }
        
        stage('Docker Test') {
            steps {
                container('docker') {
                    sh '''
                        echo "🐳 Testing Docker in dynamic agent"
                        docker --version
                        echo "Docker is working in the dynamic agent pod!"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Pipeline completed - agent pod will be cleaned up"
        }
    }
}