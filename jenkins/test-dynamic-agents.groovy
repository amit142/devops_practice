pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins: slave
    test: dynamic-agent
spec:
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
  - name: kubectl
    image: bitnami/kubectl:latest
    command: ['sleep']
    args: ['99d']
    resources:
      requests:
        memory: "64Mi"
        cpu: "50m"
      limits:
        memory: "128Mi"
        cpu: "100m"
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    
    environment {
        TEST_MESSAGE = "Hello from Dynamic Agent!"
        BUILD_INFO = "${BUILD_NUMBER}-${JOB_NAME}"
    }
    
    stages {
        stage('Agent Info') {
            steps {
                script {
                    echo "🚀 Starting test pipeline with dynamic Kubernetes agent"
                    echo "Build: ${BUILD_INFO}"
                    echo "Node: ${NODE_NAME}"
                }
                
                container('busybox') {
                    echo "📦 Running in BusyBox container"
                    sh '''
                        echo "Container hostname: $(hostname)"
                        echo "Container IP: $(hostname -i)"
                        echo "Available memory:"
                        cat /proc/meminfo | head -3
                        echo "CPU info:"
                        cat /proc/cpuinfo | grep "model name" | head -1
                        echo "Disk space:"
                        df -h
                    '''
                }
            }
        }
        
        stage('Test Multiple Containers') {
            parallel {
                stage('BusyBox Tasks') {
                    steps {
                        container('busybox') {
                            echo "🔧 Running tasks in BusyBox"
                            sh '''
                                echo "Creating test files..."
                                echo "${TEST_MESSAGE}" > /tmp/test.txt
                                echo "File created: $(cat /tmp/test.txt)"
                                
                                echo "Network test:"
                                nslookup kubernetes.default.svc.cluster.local || echo "DNS lookup failed"
                                
                                echo "Process list:"
                                ps aux
                            '''
                        }
                    }
                }
                
                stage('Docker Tasks') {
                    steps {
                        container('docker') {
                            echo "🐳 Testing Docker functionality"
                            sh '''
                                echo "Docker version:"
                                docker --version
                                
                                echo "Docker info:"
                                docker info | head -10
                                
                                echo "Pulling a small image for test:"
                                docker pull hello-world:latest
                                
                                echo "Running hello-world container:"
                                docker run --rm hello-world
                                
                                echo "Cleaning up:"
                                docker system prune -f
                            '''
                        }
                    }
                }
                
                stage('Kubectl Tasks') {
                    steps {
                        container('kubectl') {
                            echo "☸️ Testing Kubernetes connectivity"
                            sh '''
                                echo "Kubectl version:"
                                kubectl version --client
                                
                                echo "Cluster info:"
                                kubectl cluster-info
                                
                                echo "Current namespace pods:"
                                kubectl get pods -n jenkins
                                
                                echo "Current pod details:"
                                kubectl get pod $HOSTNAME -n jenkins -o yaml | head -20
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Resource Monitoring') {
            steps {
                container('busybox') {
                    echo "📊 Monitoring agent pod resources"
                    sh '''
                        echo "=== Resource Usage ==="
                        echo "Memory usage:"
                        cat /proc/meminfo | grep -E "(MemTotal|MemFree|MemAvailable)"
                        
                        echo "CPU usage:"
                        cat /proc/loadavg
                        
                        echo "Disk usage:"
                        df -h /tmp
                        
                        echo "Network interfaces:"
                        cat /proc/net/dev | head -3
                        
                        echo "Environment variables:"
                        env | grep -E "(JENKINS|KUBERNETES|POD)" | sort
                    '''
                }
            }
        }
        
        stage('Simulate Work') {
            steps {
                container('busybox') {
                    echo "⏳ Simulating some work to keep agent alive longer"
                    sh '''
                        echo "Starting work simulation..."
                        for i in $(seq 1 10); do
                            echo "Work iteration $i/10"
                            sleep 2
                            echo "  - Processing data..."
                            echo "  - Current time: $(date)"
                        done
                        echo "Work simulation completed!"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Pipeline cleanup"
            script {
                echo "Agent pod will be terminated after this step"
                echo "Total build time: ${currentBuild.durationString}"
            }
        }
        success {
            echo "✅ Dynamic agent test completed successfully!"
        }
        failure {
            echo "❌ Dynamic agent test failed!"
        }
    }
}