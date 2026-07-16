pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        DOCKER_IMAGE = 'karthi045/cie4-java-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        KUBECONFIG = '/tmp/kube-config/config'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
    }

    triggers {
        cron('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the application with Maven...'
                sh 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && export PATH=$JAVA_HOME/bin:$PATH && mvn clean compile -B'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && export PATH=$JAVA_HOME/bin:$PATH && mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && export PATH=$JAVA_HOME/bin:$PATH && mvn package -DskipTests -B'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Docker Push') {
            steps {
                echo 'Pushing image to DockerHub...'
                sh "echo \${DOCKERHUB_CREDENTIALS_PSW} | docker login -u \${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy to Minikube') {
            steps {
                echo 'Deploying to Minikube...'
                sh '''
                    if [ ! -x kubectl ]; then
                        curl -LO https://dl.k8s.io/release/v1.31.0/bin/linux/amd64/kubectl
                        chmod +x kubectl
                    fi
                    export KUBECONFIG=/tmp/kube-config/config
                    ./kubectl get nodes
                    ./kubectl apply -f k8s/deployment.yaml
                    ./kubectl apply -f k8s/service.yaml
                    ./kubectl rollout status deployment/cie4-java-app --timeout=120s
                    ./kubectl get pods -l app=cie4-java-app
                    ./kubectl get svc cie4-java-app-service
                '''
            }
        }

        stage('Verify') {
            steps {
                echo 'Verifying deployment...'
                sh '''
                    export KUBECONFIG=/tmp/kube-config/config
                    MINIKUBE_IP=$(./kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
                    echo "Application URL: http://$MINIKUBE_IP:30080/api/health"
                    for i in 1 2 3 4 5; do
                        if curl -s "http://$MINIKUBE_IP:30080/api/health"; then
                            echo "Application is healthy!"
                            break
                        fi
                        echo "Waiting for application to be ready... attempt $i"
                        sleep 10
                    done
                '''
            }
        }
    }

    post {
        success {
            echo 'CI/CD Pipeline completed successfully!'
        }
        failure {
            echo 'CI/CD Pipeline failed!'
        }
        always {
            sh 'docker logout || true'
        }
    }
}
