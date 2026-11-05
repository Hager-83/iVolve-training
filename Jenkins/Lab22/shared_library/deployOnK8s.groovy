
def call(Map config = [:]) {

    String namespace = config.get('namespace', 'default')
    String deployment = config.deployment
    String container = config.container
    String manifest = config.get('manifest', 'kubernetes/deployment.yaml')
    String credentialsId = config.get('kubeCredentials', 'kubeconfig-ivolve')
    String kubectl = config.get('kubectl', 'kubectl')

    if (!deployment?.trim() || !container?.trim()) {
        error('deployment and container are required')
    }

    if (!env.FULL_IMAGE?.trim()) {
        error('FULL_IMAGE is missing')
    }

    withEnv([
        "DEPLOY_NAMESPACE=${namespace}",
        "DEPLOY_NAME=${deployment}",
        "DEPLOY_CONTAINER=${container}",
        "DEPLOY_MANIFEST=${manifest}",
        "KUBECTL_BIN=${kubectl}"
    ]) {

        withCredentials([
            file(
                credentialsId: credentialsId,
                variable: 'KUBECONFIG'
            )
        ]) {

            sh '''
                set -e

                echo "Updating deployment image..."

                python3 - <<'PY'
import os
import re
from pathlib import Path

path = Path(os.environ["DEPLOY_MANIFEST"])
content = path.read_text()

container = re.escape(os.environ["DEPLOY_CONTAINER"])
image = os.environ["FULL_IMAGE"]

pattern = (
    r"(?m)^(\\s*-\\s*name:\\s*" + container +
    r"\\s*\\n\\s*image:\\s*)\\S+"
)

updated, count = re.subn(
    pattern,
    lambda match: match.group(1) + image,
    content
)

if count != 1:
    raise SystemExit(
        f"Expected one matching container, found {count}"
    )

path.write_text(updated)
print(f"Updated {path} with {image}")
PY

                echo "Applying Kubernetes deployment..."

                "$KUBECTL_BIN" apply \
                  -f "$DEPLOY_MANIFEST" \
                  -n "$DEPLOY_NAMESPACE"

                echo "Checking rollout..."

                "$KUBECTL_BIN" rollout status \
                  deployment/"$DEPLOY_NAME" \
                  -n "$DEPLOY_NAMESPACE" \
                  --timeout=300s
            '''
        }
    }

    echo "Deployment completed: ${deployment}"
}
