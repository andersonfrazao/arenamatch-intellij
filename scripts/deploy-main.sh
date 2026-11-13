#!/usr/bin/env bash
# Executado pela pipeline da main; reutiliza o Compose existente de producao.
set -euo pipefail
umask 077

if [[ $# -ne 6 && ! ( $# -eq 7 && ${7} == --check ) ]]; then
  echo 'Uso: deploy-main.sh DIRETORIO COMPOSE PROJETO APP BANCO COMMIT [--check]' >&2
  exit 1
fi
deploy_path=$1
compose_file=$2
project=$3
service=$4
database_service=$5
commit=$6
[[ "$commit" =~ ^[0-9a-f]{40}$ ]]
[[ "$project" =~ ^[a-z0-9][a-z0-9_-]*$ ]]
[[ "$service" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]*$ ]]
[[ "$database_service" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]*$ ]]
[[ "$service" != "$database_service" ]]
image="arenamatch-main:$commit"

for command in docker python3 flock; do
  command -v "$command" >/dev/null || { echo "Instale $command no servidor." >&2; exit 1; }
done
cd -- "$deploy_path"
test -f "$compose_file"
compose=(docker compose --project-name "$project" --file "$compose_file")

"${compose[@]}" config --format json | python3 -c '
import json, sys
config = json.load(sys.stdin)
service = config.get("services", {}).get(sys.argv[1], {})
profile = service.get("environment", {}).get("SPRING_PROFILES_ACTIVE", "")
if profile != "prod":
    sys.exit("Deploy recusado: o servico deve declarar SPRING_PROFILES_ACTIVE=prod no Compose.")
' "$service"

container=$("${compose[@]}" ps --all --quiet "$service")
database_container=$("${compose[@]}" ps --quiet "$database_service")
if [[ -z "$container" || "$container" == *$'\n'* || -z "$database_container" || "$database_container" == *$'\n'* ]]; then
  echo 'Producao deve ter exatamente um container de app e um banco em execucao.' >&2
  exit 1
fi
docker inspect --format '{{json .Config.Env}}' "$container" | python3 -c '
import json, sys
if "SPRING_PROFILES_ACTIVE=prod" not in json.load(sys.stdin):
    sys.exit("Deploy recusado: o container existente nao esta no perfil prod.")
'
docker exec "$database_container" sh -c \
  'test -n "$POSTGRES_USER" && test -n "$POSTGRES_DB" && command -v pg_dump && command -v pg_restore' >/dev/null
if [[ ${7:-} == --check ]]; then
  echo "Preflight producao OK: projeto=$project app=$service banco=$database_service diretorio=$PWD"
  exit 0
fi

mkdir -p .deploy-main/backups
exec 9>.deploy-main/deploy.lock
flock -n 9 || { echo 'Outro deploy de producao esta em andamento.' >&2; exit 1; }
previous_image=$(docker inspect --format '{{.Image}}' "$container")
docker image inspect "$image" >/dev/null

# Backup consistente antes de iniciar o novo app e suas migracoes Flyway.
backup_file=".deploy-main/backups/$(date -u +%Y%m%dT%H%M%SZ)-$commit.dump"
docker exec "$database_container" sh -c \
  'exec pg_dump --format=custom --username="$POSTGRES_USER" --dbname="$POSTGRES_DB"' > "$backup_file"
test -s "$backup_file"
docker exec -i "$database_container" pg_restore --list < "$backup_file" >/dev/null
echo "Backup PostgreSQL criado e catalogo validado: $PWD/$backup_file"

override=.deploy-main/compose.override.yml
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
  printf '%s\n' "$previous_image" > .deploy-main/previous-image
  printf '%s\n' "$commit" > .deploy-main/current-commit
  echo "Deploy producao concluido: $commit"
else
  echo 'Deploy falhou. Restaurando a imagem anterior do aplicativo.' >&2
  write_override "$previous_image"
  if "${compose[@]}" up --detach --no-deps --no-build --pull never "$service" && ready; then
    echo 'Imagem anterior restaurada.' >&2
  else
    echo 'A restauracao tambem falhou. Verifique o servico de producao.' >&2
  fi
  echo "Backup de banco preservado em $PWD/$backup_file; banco nao restaurado automaticamente." >&2
  exit 1
fi
