#!/usr/bin/env bash
# Executado no servidor pela pipeline; reutiliza o Compose existente do dev.
set -euo pipefail

if [[ $# -ne 5 && ! ( $# -eq 6 && ${6} == --check ) ]]; then
  echo 'Uso: deploy-dev.sh DIRETORIO COMPOSE PROJETO SERVICO COMMIT [--check]' >&2
  exit 1
fi
deploy_path=$1
compose_file=$2
project=$3
service=$4
commit=$5
[[ "$commit" =~ ^[0-9a-f]{40}$ ]]
[[ "$project" =~ ^[a-z0-9][a-z0-9_-]*$ ]]
[[ "$service" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]*$ ]]
image="arenamatch-dev:$commit"

for command in docker python3 flock; do
  command -v "$command" >/dev/null || { echo "Instale $command no servidor." >&2; exit 1; }
done
cd -- "$deploy_path"
test -f "$compose_file"
compose=(docker compose --project-name "$project" --file "$compose_file")

# Falha antes de recriar containers se o alvo nao estiver explicitamente em dev.
"${compose[@]}" config --format json | python3 -c '
import json, sys
config = json.load(sys.stdin)
service = config.get("services", {}).get(sys.argv[1], {})
profile = service.get("environment", {}).get("SPRING_PROFILES_ACTIVE", "")
if profile != "dev":
    sys.exit("Deploy recusado: o servico deve declarar SPRING_PROFILES_ACTIVE=dev no Compose.")
' "$service"

container=$("${compose[@]}" ps --all --quiet "$service")
if [[ -z "$container" || "$container" == *$'\n'* ]]; then
  echo 'O servico dev deve existir e ter exatamente um container. Confira projeto e servico.' >&2
  exit 1
fi
docker inspect --format '{{json .Config.Env}}' "$container" | python3 -c '
import json, sys
if "SPRING_PROFILES_ACTIVE=dev" not in json.load(sys.stdin):
    sys.exit("Deploy recusado: o container existente nao esta no perfil dev.")
'
previous_image=$(docker inspect --format '{{.Image}}' "$container")
if [[ ${6:-} == --check ]]; then
  echo "Preflight dev OK: projeto=$project servico=$service diretorio=$PWD"
  exit 0
fi
mkdir -p .deploy-dev
exec 9>.deploy-dev/deploy.lock
flock -n 9 || { echo 'Outro deploy dev esta em andamento.' >&2; exit 1; }
# Rele a imagem depois do lock para evitar restaurar uma versao desatualizada.
previous_image=$(docker inspect --format '{{.Image}}' "$container")
docker image inspect "$image" >/dev/null
override=.deploy-dev/compose.override.yml
compose+=(--file "$override")
write_override() {
  printf 'services:\n  %s:\n    image: "%s"\n' "$service" "$1" > "$override"
}
ready() {
  local current
  for ((attempt = 0; attempt < 60; attempt++)); do
    current=$("${compose[@]}" ps --quiet "$service")
    if [[ -n "$current" ]] && docker exec "$current" \
      wget -q -T 5 -O /dev/null http://127.0.0.1:8080/login.xhtml; then
      return 0
    fi
    sleep 3
  done
  return 1
}

write_override "$image"
if "${compose[@]}" up --detach --no-deps --no-build --pull never "$service" && ready; then
  printf '%s\n' "$previous_image" > .deploy-dev/previous-image
  printf '%s\n' "$commit" > .deploy-dev/current-commit
  echo "Deploy dev concluido: $commit"
else
  echo 'Deploy falhou. Restaurando a imagem anterior do aplicativo.' >&2
  write_override "$previous_image"
  if "${compose[@]}" up --detach --no-deps --no-build --pull never "$service" && ready; then
    echo 'Imagem anterior restaurada.' >&2
  else
    echo 'A restauracao tambem falhou. Verifique o servico dev no servidor.' >&2
  fi
  exit 1
fi
