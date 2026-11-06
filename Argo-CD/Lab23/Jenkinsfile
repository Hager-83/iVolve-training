
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

        stage('Update GitOps Manifest') {
            steps {
                updateGitOpsManifest(
                    manifest: 'kubernetes/deployment.yaml',
                    container: 'ivolve-app',
                    gitCredentials: 'github-private-repos'
                )
            }
        }
    }

    post {
        success {
            echo "SUCCESS: ${env.FULL_IMAGE} pushed to Docker Hub and GitOps manifest updated."
            echo "Argo CD is responsible for Kubernetes deployment."
        }

        failure {
            echo 'FAILURE: Check Jenkins console output.'
        }

        always {
            echo "Pipeline finished. Build: ${env.BUILD_NUMBER}"

            sh 'docker logout >/dev/null 2>&1 || true'

            // cleanWs()
        }
    }
}
