#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# Only source your own trusted local configuration. Never source downloaded .env files.
if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi
exec ./gradlew :app:assembleStandardRelease :app:assembleDemoRelease \
  -PincludeX86=true \
  "-PcoachEndpoint=${FIRESPOT_COACH_ENDPOINT:-}" \
  "-PcloudEndpoint=${FIRESPOT_CLOUD_ENDPOINT:-}" \
  "-PcognitoRegion=${FIRESPOT_COGNITO_REGION:-}" \
  "-PcognitoClientId=${FIRESPOT_COGNITO_CLIENT_ID:-}" \
  "$@"
