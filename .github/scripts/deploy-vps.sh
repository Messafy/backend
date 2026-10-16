#!/usr/bin/env bash
set -euo pipefail

image=${1:?Missing commit image}
deploy_dir=${2:?Missing deployment directory}
health_path=/actuator/health/readiness
container=messafy-backend
backup=messafy-backend-previous
health_url="http://127.0.0.1:8080${health_path}"

# shellcheck disable=SC2032,SC2033 # sudo invokes the Docker executable, not this shell wrapper.
docker() { sudo -n docker "$@"; }

fail() { printf '%s\n' "$1" >&2; exit 1; }

[[ "$image" =~ ^messafy/backend:[0-9a-f]{40}$ ]] || fail 'Invalid image name.'
[[ "$health_path" =~ ^/[a-zA-Z0-9/_-]*$ ]] || fail 'Invalid health path.'

cd "$deploy_dir"
[[ -r .env ]] || fail 'Missing readable VPS .env file.'
command -v curl > /dev/null || fail 'curl is required.'

if docker container inspect "$backup" > /dev/null 2>&1; then
  fail 'Previous backup already exists; inspect it before deploying.'
fi

# The workflow loads the image over SSH before invoking this script.
# A missing image must not interrupt the currently running application.
docker image inspect "$image" > /dev/null || fail 'Image is not loaded on the VPS.'

had_previous=0
prev_image=
if docker container inspect "$container" > /dev/null 2>&1; then
  had_previous=1
  prev_image=$(docker inspect --format '{{.Config.Image}}' "$container")
else
  printf '%s\n' 'No existing container found; performing first deployment.'
fi

# State-based rollback: it only acts on what actually exists, so it is safe to
# arm the trap before touching anything.
# shellcheck disable=SC2329 # Invoked by the EXIT trap.
rollback() {
  local result=$?
  trap - EXIT INT TERM
  printf 'Deployment failed (exit %s).\n' "$result" >&2
  docker logs --tail 50 "$container" >&2 2>&1 || true
  if docker container inspect "$backup" > /dev/null 2>&1; then
    printf '%s\n' 'Restoring previous container.' >&2
    docker rm -f "$container" > /dev/null 2>&1 || true
    if docker rename "$backup" "$container" && docker start "$container" > /dev/null; then
      printf '%s\n' 'Previous container restarted; verify its health.' >&2
    else
      printf '%s\n' 'ROLLBACK FAILED: manual intervention required.' >&2
    fi
  elif ((had_previous)); then
    printf '%s\n' 'Previous container was not touched.' >&2
  else
    docker rm -f "$container" > /dev/null 2>&1 || true
  fi
  exit "$result"
}

trap rollback EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

if ((had_previous)); then
  docker rename "$container" "$backup"
  docker stop --time 30 "$backup" > /dev/null
fi

docker run --detach \
  --pull never \
  --name "$container" \
  --restart unless-stopped \
  --network bridge \
  --publish 127.0.0.1:8080:8080 \
  --env-file .env \
  --env SPRING_PROFILES_ACTIVE=prod \
  "$image" > /dev/null

# Require consecutive good responses to avoid accepting a transient startup response.
ready=0
for ((attempt = 1; attempt <= 30; attempt++)); do
  running=$(docker inspect --format '{{.State.Running}}' "$container")
  [[ "$running" == true ]] || fail 'New container exited before HTTP readiness.'

  status=$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --connect-timeout 2 --max-time 3 "$health_url") || status=000

  good=0
  if [[ "$status" == 200 ]]; then
    good=1
  fi

  if ((good)); then
    ready=$((ready + 1))
    if ((ready >= 3)); then
      # From here on the deployment is good: cleanup must never trigger a rollback.
      trap - EXIT INT TERM
      if ((had_previous)); then
        docker rm "$backup" > /dev/null 2>&1 \
          || printf '%s\n' 'Warning: could not remove backup container.' >&2
      fi
      # Keep the current and previous images; remove older ones (rmi refuses in-use images).
      while IFS= read -r old; do
        if [[ "$old" != "$image" && "$old" != "$prev_image" ]]; then
          docker rmi "$old" > /dev/null 2>&1 || true
        fi
      done < <(docker images --format '{{.Repository}}:{{.Tag}}' messafy/backend)
      printf 'Deployed %s (HTTP %s).\n' "$image" "$status"
      exit 0
    fi
  else
    ready=0
  fi
  sleep 5
done

fail 'New container did not become HTTP-ready.'
