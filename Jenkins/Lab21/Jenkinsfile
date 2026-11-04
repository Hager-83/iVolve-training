
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        IMAGE_REPO = 'hager83/ivolve-app'
        KUBE_NAMESPACE = 'ivolve'
        DEPLOYMENT_NAME = 'ivolveapp'
        KUBECTL = '/usr/local/bin/kubectl-1.35'
    }

    stages {
        stage('Build App') {
            steps {
                checkout scm

                dir('app') {
                    sh 'npm ci'
                }

                script {
                    def shortCommit = sh(
                        script: 'git rev-parse --short=7 HEAD',
                        returnStdout: true
                    ).trim()

                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"
                    env.FULL_IMAGE = "${env.IMAGE_REPO}:${env.IMAGE_TAG}"
                }

                echo "Building image: ${env.FULL_IMAGE}"
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t "$FULL_IMAGE" .'
            }
        }

        stage('Scan Docker Image') {
            steps {
                sh '''
                    trivy image \
                      --exit-code 1 \
                      --severity HIGH,CRITICAL \
                      --no-progress \
                      "$FULL_IMAGE"
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x
                        printf '%s' "$DOCKER_TOKEN" | docker login \
                          --username "$DOCKER_USER" \
                          --password-stdin

                        docker push "$FULL_IMAGE"
                        docker logout
                    '''
                }
            }
        }

        stage('Remove Local Image') {
            steps {
                sh 'docker image rm "$FULL_IMAGE"'
            }
        }

        stage('Update Deployment YAML') {
            steps {
                sh '''
                    sed -i \
                      "s|image: hager83/ivolve-app:.*|image: $FULL_IMAGE|" \
                      kubernetes/deployment.yaml

                    grep -n 'image:' kubernetes/deployment.yaml
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    file(
                        credentialsId: 'kubeconfig-ivolve',
                        variable: 'KUBECONFIG'
                    )
                ]) {
                    sh '''
                        "$KUBECTL" apply \
                          -f kubernetes/deployment.yaml \
                          -n "$KUBE_NAMESPACE"

                        "$KUBECTL" rollout status \
                          deployment/"$DEPLOYMENT_NAME" \
                          -n "$KUBE_NAMESPACE" \
                          --timeout=300s
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished. Build: ${env.BUILD_NUMBER}"
            sh 'docker logout >/dev/null 2>&1 || true'
            cleanWs()
        }

        success {
            echo "SUCCESS: ${env.FULL_IMAGE} deployed to Kubernetes."
        }

        failure {
            echo "FAILURE: Check the Jenkins console output."
        }
    }
}
