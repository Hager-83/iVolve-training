
def call(Map config = [:]) {

    String appDir = config.get('appDir', '.')
    String buildCommand = config.get('buildCommand', 'npm ci')

    checkout scm

    dir(appDir) {
        sh buildCommand
    }

    String shortCommit = sh(
        script: 'git rev-parse --short=7 HEAD',
        returnStdout: true
    ).trim()

    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"

    echo "Application build completed."
    echo "Image tag: ${env.IMAGE_TAG}"
}
