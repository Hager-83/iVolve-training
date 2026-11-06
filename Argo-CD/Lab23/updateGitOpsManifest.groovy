
def call(Map config = [:]) {

    String manifest = config.get('manifest', 'kubernetes/deployment.yaml')
    String container = config.get('container', 'ivolve-app')
    String credentialsId = config.get('gitCredentials', 'github-private-repos')

    if (!env.FULL_IMAGE?.trim()) {
        error('FULL_IMAGE is missing')
    }

    withEnv([
        "DEPLOY_MANIFEST=${manifest}",
        "DEPLOY_CONTAINER=${container}"
    ]) {

        sh '''
            set -eu

            echo "Updating Kubernetes manifest for GitOps..."

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

            git config user.name "Jenkins GitOps Bot"
            git config user.email "jenkins-gitops@users.noreply.github.com"

            git add "$DEPLOY_MANIFEST"

            if git diff --cached --quiet; then
                echo "No manifest changes to commit."
                exit 0
            fi

            git commit -m "chore(gitops): update image [skip ci]"
        '''

        withCredentials([
            usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'GIT_USERNAME',
                passwordVariable: 'GIT_TOKEN'
            )
        ]) {
            sh '''
                set -eu
                set +x

                ASKPASS_SCRIPT="$(mktemp)"
                trap 'rm -f "$ASKPASS_SCRIPT"' EXIT

                cat > "$ASKPASS_SCRIPT" <<'EOF'
#!/bin/sh
case "$1" in
    *Username*) printf '%s\\n' "$GIT_USERNAME" ;;
    *Password*) printf '%s\\n' "$GIT_TOKEN" ;;
esac
EOF

                chmod 700 "$ASKPASS_SCRIPT"

                echo "Pushing GitOps manifest to GitHub..."

                GIT_ASKPASS="$ASKPASS_SCRIPT" \
                GIT_TERMINAL_PROMPT=0 \
                git -c credential.helper= push origin HEAD:main
            '''
        }
    }

    echo "GitOps manifest pushed: ${env.FULL_IMAGE}"
}
