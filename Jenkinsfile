pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        DOCKER_IMAGE = 'karthi045/cie4-java-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH = "/tmp/minikube-bin:${env.JAVA_HOME}/bin:${env.PATH}"
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
                sh 'mvn clean compile -B'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test -B'
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
                sh 'mvn package -DskipTests -B'
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
                    if ! minikube status | grep -q "Running"; then
                        minikube start --driver=docker
                    fi
                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml
                    kubectl rollout status deployment/cie4-java-app --timeout=120s
                '''
            }
        }

        stage('Verify') {
            steps {
                echo 'Verifying deployment...'
                sh '''
                    MINIKUBE_IP=$(minikube ip)
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
