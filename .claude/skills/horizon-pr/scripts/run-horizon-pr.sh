#!/usr/bin/env bash
# Download the Horizon container image that CircleCI built for a PR or branch,
# load it into Docker and start it with PostgreSQL through docker compose.
set -euo pipefail

REPO_SLUG="gh/OpenNMS/opennms"
GH_REPO="OpenNMS/opennms"
IMAGE_JOB="horizon-image-single-arch-linux-amd64"
ARTIFACT="horizon-linux-amd64.oci"
API="https://circleci.com/api/v2"
CACHE_DIR="${HORIZON_PR_CACHE:-$HOME/.cache/opennms-horizon-pr}"
SKILL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$SKILL_DIR/assets/docker-compose.yml"

usage() {
  cat <<USAGE
Usage: $(basename "$0") <pr-number|branch> [options]

Options:
  --resolve-only       Print pipeline, job and artifact URL, then exit.
  --no-start           Download and load the image, do not start compose.
  --down               Stop and remove the compose stack and volumes for this ref.
  --pipeline <number>  Use this CircleCI pipeline number instead of the newest.
  --web-port <port>    Host port for the web UI (default 18980).
  --ssh-port <port>    Host port for the Karaf shell (default 18101).
  --wait <seconds>     How long to wait for the web UI (default 900).
  -h, --help           Show this help.

Environment:
  CIRCLE_TOKEN or CIRCLECI_TOKEN   CircleCI API token, needed for the artifact download.
                                   Falls back to the file ~/.circleci/token.
  HORIZON_PR_CACHE                 Download cache (default ~/.cache/opennms-horizon-pr).
  HORIZON_TRAP_PORT, HORIZON_SYSLOG_PORT   Host UDP ports (default 11162, 20514).
USAGE
}

log() { printf '==> %s\n' "$*" >&2; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

REF=""; RESOLVE_ONLY=0; NO_START=0; DOWN=0; PIPELINE_NUMBER=""; WEB_PORT="${HORIZON_WEB_PORT:-18980}"; SSH_PORT="${HORIZON_SSH_PORT:-18101}"; WAIT_SECS=900
while [ $# -gt 0 ]; do
  case "$1" in
    --resolve-only) RESOLVE_ONLY=1 ;;
    --no-start) NO_START=1 ;;
    --down) DOWN=1 ;;
    --pipeline) PIPELINE_NUMBER="$2"; shift ;;
    --web-port) WEB_PORT="$2"; shift ;;
    --ssh-port) SSH_PORT="$2"; shift ;;
    --wait) WAIT_SECS="$2"; shift ;;
    -h|--help) usage; exit 0 ;;
    -*) die "unknown option: $1" ;;
    *) [ -z "$REF" ] || die "only one PR number or branch is allowed"; REF="$1" ;;
  esac
  shift
done
[ -n "$REF" ] || { usage >&2; exit 1; }

for tool in curl jq docker; do
  command -v "$tool" >/dev/null || die "$tool is required"
done

TOKEN="${CIRCLE_TOKEN:-${CIRCLECI_TOKEN:-}}"
if [ -z "$TOKEN" ] && [ -s "$HOME/.circleci/token" ]; then
  TOKEN="$(tr -d '[:space:]' < "$HOME/.circleci/token")"
fi
api() { curl -fsS ${TOKEN:+-H "Circle-Token: $TOKEN"} "$API/$1"; }

# --- resolve ref -> branch + compose project name ---------------------------
if [[ "$REF" =~ ^[0-9]+$ ]]; then
  command -v gh >/dev/null || die "gh is required to resolve PR #$REF"
  PR_JSON="$(gh pr view "$REF" --repo "$GH_REPO" --json headRefName,headRepositoryOwner,title)"
  BRANCH="$(jq -r .headRefName <<<"$PR_JSON")"
  OWNER="$(jq -r .headRepositoryOwner.login <<<"$PR_JSON")"
  log "PR #$REF: $(jq -r .title <<<"$PR_JSON")"
  [ "$OWNER" = "OpenNMS" ] || log "warning: PR head is on fork '$OWNER'; CircleCI may not have built it"
  PROJECT="horizon-pr-$REF"
else
  BRANCH="$REF"
  PROJECT="horizon-pr-$(tr -c 'a-zA-Z0-9\n' '-' <<<"$REF" | tr 'A-Z' 'a-z' | sed -e 's/^-*//' -e 's/-*$//')"
fi
log "branch: $BRANCH"

if [ "$DOWN" = 1 ]; then
  log "removing compose project $PROJECT (including volumes)"
  docker compose -p "$PROJECT" -f "$COMPOSE_FILE" down -v
  exit 0
fi

# --- find newest pipeline with a successful image job -----------------------
ENC_BRANCH="$(jq -rn --arg b "$BRANCH" '$b|@uri')"
PIPELINES="$(api "project/$REPO_SLUG/pipeline?branch=$ENC_BRANCH" | jq -c '.items[]')"
[ -n "$PIPELINES" ] || die "no CircleCI pipelines found for branch '$BRANCH'"

JOB_NUMBER=""; FOUND_PIPELINE=""; FOUND_REV=""; FOUND_WORKFLOW=""
while read -r p; do
  pid="$(jq -r .id <<<"$p")"; pnum="$(jq -r .number <<<"$p")"; rev="$(jq -r .vcs.revision <<<"$p")"
  if [ -n "$PIPELINE_NUMBER" ] && [ "$pnum" != "$PIPELINE_NUMBER" ]; then continue; fi
  for wid in $(api "pipeline/$pid/workflow" | jq -r '.items[].id'); do
    jn="$(api "workflow/$wid/job" | jq -r --arg n "$IMAGE_JOB" '.items[] | select(.name==$n and .status=="success") | .job_number' | head -1)"
    if [ -n "$jn" ]; then JOB_NUMBER="$jn"; FOUND_PIPELINE="$pnum"; FOUND_REV="$rev"; FOUND_WORKFLOW="$wid"; break 2; fi
  done
  [ -z "$PIPELINE_NUMBER" ] || break
done <<<"$PIPELINES"

if [ -z "$JOB_NUMBER" ]; then
  newest="$(head -1 <<<"$PIPELINES" | jq -r .number)"
  die "no successful '$IMAGE_JOB' job on branch '$BRANCH' yet. Newest pipeline: https://app.circleci.com/pipelines/github/OpenNMS/opennms/$newest"
fi

ARTIFACT_URL="$(api "project/$REPO_SLUG/$JOB_NUMBER/artifacts" | jq -r --arg a "$ARTIFACT" '.items[] | select(.path==$a) | .url')"
[ -n "$ARTIFACT_URL" ] || die "job $JOB_NUMBER has no artifact '$ARTIFACT'"

log "pipeline $FOUND_PIPELINE, commit ${FOUND_REV:0:10}, job $JOB_NUMBER"
log "job: https://app.circleci.com/pipelines/github/OpenNMS/opennms/$FOUND_PIPELINE/workflows/$FOUND_WORKFLOW/jobs/$JOB_NUMBER"
log "artifact: $ARTIFACT_URL"
[ "$RESOLVE_ONLY" = 0 ] || exit 0

# --- download + load ---------------------------------------------------------
IMAGE_TAG="opennms/horizon:ci-$JOB_NUMBER"
REF_TAG="opennms/horizon:${PROJECT#horizon-}"
if docker image inspect "$IMAGE_TAG" >/dev/null 2>&1; then
  log "image $IMAGE_TAG already loaded"
else
  [ -n "$TOKEN" ] || die "set CIRCLE_TOKEN or put a CircleCI personal API token in ~/.circleci/token; artifact downloads need it"
  DEST_DIR="$CACHE_DIR/$JOB_NUMBER"; mkdir -p "$DEST_DIR"
  DEST="$DEST_DIR/$ARTIFACT"
  if [ ! -s "$DEST" ]; then
    log "downloading to $DEST"
    curl -fL --retry 3 -C - -H "Circle-Token: $TOKEN" -o "$DEST.part" "$ARTIFACT_URL"
    mv "$DEST.part" "$DEST"
  else
    log "using cached $DEST"
  fi
  log "loading image into Docker"
  LOAD_OUT="$(docker image load -i "$DEST")"
  echo "$LOAD_OUT" >&2
  LOADED="$(sed -n 's/^Loaded image: //p' <<<"$LOAD_OUT" | head -1)"
  [ -n "$LOADED" ] || die "could not find the loaded image tag in 'docker image load' output"
  docker image tag "$LOADED" "$IMAGE_TAG"
fi
docker image tag "$IMAGE_TAG" "$REF_TAG"
log "image tags: $IMAGE_TAG, $REF_TAG"
[ "$NO_START" = 0 ] || exit 0

# --- start -------------------------------------------------------------------
TRAP_PORT="${HORIZON_TRAP_PORT:-11162}"; SYSLOG_PORT="${HORIZON_SYSLOG_PORT:-20514}"
busy=""
for spec in "tcp:$WEB_PORT:--web-port" "tcp:$SSH_PORT:--ssh-port" "udp:$TRAP_PORT:HORIZON_TRAP_PORT" "udp:$SYSLOG_PORT:HORIZON_SYSLOG_PORT"; do
  proto="${spec%%:*}"; rest="${spec#*:}"; port="${rest%%:*}"; knob="${rest#*:}"
  if [ "$proto" = tcp ]; then used="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true)"; else used="$(lsof -nP -iUDP:"$port" -t 2>/dev/null || true)"; fi
  if [ -n "$used" ] && ! docker compose -p "$PROJECT" -f "$COMPOSE_FILE" ps -q horizon 2>/dev/null | grep -q .; then
    busy="$busy
  $proto $port is in use by: $(ps -o comm= -p "$(head -1 <<<"$used")" 2>/dev/null || echo "pid $used")  (change with $knob)"
  fi
done
[ -z "$busy" ] || die "host ports are already in use, probably another horizon-pr stack:$busy"

log "starting compose project $PROJECT on port $WEB_PORT"
HORIZON_IMAGE="$IMAGE_TAG" HORIZON_WEB_PORT="$WEB_PORT" HORIZON_SSH_PORT="$SSH_PORT" \
HORIZON_TRAP_PORT="$TRAP_PORT" HORIZON_SYSLOG_PORT="$SYSLOG_PORT" \
  docker compose -p "$PROJECT" -f "$COMPOSE_FILE" up -d

URL="http://localhost:$WEB_PORT/opennms/"
log "waiting up to ${WAIT_SECS}s for $URL (amd64 under emulation is slow)"
deadline=$(( $(date +%s) + WAIT_SECS ))
until curl -fsS -o /dev/null "${URL}login.jsp" 2>/dev/null; do
  if ! docker compose -p "$PROJECT" -f "$COMPOSE_FILE" ps --status running --services | grep -q '^horizon$'; then
    docker compose -p "$PROJECT" -f "$COMPOSE_FILE" logs --tail 50 horizon >&2
    die "horizon container is not running"
  fi
  [ "$(date +%s)" -lt "$deadline" ] || die "web UI did not answer within ${WAIT_SECS}s; check: docker compose -p $PROJECT -f $COMPOSE_FILE logs -f horizon"
  sleep 10
done

cat <<DONE
Horizon is up: $URL  (admin / admin)
Image: $IMAGE_TAG  (pipeline $FOUND_PIPELINE, commit ${FOUND_REV:0:10})
Logs:  docker compose -p $PROJECT -f $COMPOSE_FILE logs -f horizon
Stop:  $0 $REF --down
DONE
