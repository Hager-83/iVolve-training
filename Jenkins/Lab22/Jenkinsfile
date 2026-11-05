
@Library('ivolve-shared-library@main') _

pipeline {
    agent {
        label 'docker-k8s'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        KUBECTL = '/usr/local/bin/kubectl'
    }

    stages {

        stage('Build App') {
            steps {
                buildApp(
                    appDir: 'app',
                    buildCommand: 'npm ci'
                )
            }
        }

        stage('Build Docker Image') {
            steps {
                buildImage(
                    imageRepo: 'hager83/ivolve-app',
                    dockerfile: 'Dockerfile',
                    context: '.',
                    registryCredentials: 'dockerhub-credentials',
                    scanSeverity: 'HIGH,CRITICAL'
                )
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                deployOnK8s(
                    namespace: 'ivolve',
                    deployment: 'ivolveapp',
                    container: 'ivolve-app',
                    manifest: 'kubernetes/deployment.yaml',
                    kubeCredentials: 'kubeconfig-ivolve-agent',
                    kubectl: env.KUBECTL
                )
            }
        }
    }

    post {
        success {
            echo "SUCCESS: ${env.FULL_IMAGE} deployed to Kubernetes."
        }

        failure {
            echo 'FAILURE: Check Jenkins console output.'
        }

        always {
            echo "Pipeline finished. Build: ${env.BUILD_NUMBER}"

            sh 'docker logout >/dev/null 2>&1 || true'

            cleanWs()
        }
    }
}
