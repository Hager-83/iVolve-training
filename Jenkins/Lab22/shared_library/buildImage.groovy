
def call(Map config = [:]) {
    String imageRepo = config.imageRepo
    String dockerfile = config.get('dockerfile', 'Dockerfile')
    String context = config.get('context', '.')
    String credentialsId = config.get(
        'registryCredentials',
        'dockerhub-credentials'
    )
    String severity = config.get(
        'scanSeverity',
        'HIGH,CRITICAL'
    )

    if (!imageRepo?.trim()) {
        error('imageRepo is required')
    }

    env.FULL_IMAGE = "${imageRepo}:${env.IMAGE_TAG}"

    sh """
        docker build \
          -f '${dockerfile}' \
          -t "\$FULL_IMAGE" \
          '${context}'
    """

    sh """
        trivy image \
          --exit-code 1 \
          --severity '${severity}' \
          --no-progress \
          "\$FULL_IMAGE"
    """

    withCredentials([
        usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_TOKEN'
        )
    ]) {
        sh '''
            set +x

            echo "$DOCKER_TOKEN" | docker login \
              --username "$DOCKER_USER" \
              --password-stdin

            docker push "$FULL_IMAGE"
            docker logout
        '''
    }

    sh 'docker image rm "$FULL_IMAGE"'

    echo "Image published: ${env.FULL_IMAGE}"
}
