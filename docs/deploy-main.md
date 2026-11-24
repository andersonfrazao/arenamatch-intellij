# Deploy automatico da main na Hetzner

Push na branch `main` executa testes Java com PostgreSQL 15 descartavel e testes
JavaScript, constroi a imagem Java 21 ARM64 e publica em producao por SSH.

Workflow: `.github/workflows/deploy-main.yml`.
Script remoto: `scripts/deploy-main.sh`.

## Destino confirmado

| Configuracao | Valor padrao |
| --- | --- |
| Host SSH | `178.105.94.89` |
| Porta SSH | `22` |
| Usuario | `root` |
| Diretorio | `/opt/arenamatch` |
| Compose | `docker-compose.yml` |
| Projeto Compose | `arenamatch` |
| Servico da aplicacao | `app` |
| Servico PostgreSQL | `db` |
| Perfil Spring | `prod` |
| Imagem publicada | `arenamatch-main:<SHA do commit>` |

O app existente e `arenamatch-app-1`, com uploads persistidos em
`/opt/arenamatch/uploads:/app/uploads`. O banco usa o volume
`arenamatch_postgres_data`. O Caddy existente continua servindo HTTPS.

As configuracoes podem ser sobrescritas pelas Repository variables
`PROD_SSH_HOST`, `PROD_SSH_PORT`, `PROD_SSH_USER`, `PROD_DEPLOY_PATH`,
`PROD_COMPOSE_FILE`, `PROD_COMPOSE_PROJECT`, `PROD_COMPOSE_SERVICE` e
`PROD_COMPOSE_DB_SERVICE`. Nao e necessario cadastrar variaveis para usar
os valores acima.

## Secrets do GitHub Actions

Em Settings -> Secrets and variables -> Actions -> Repository secrets:

| Secret | Conteudo |
| --- | --- |
| `PROD_SSH_PRIVATE_KEY` | Chave privada exclusiva da automacao de producao |
| `PROD_SSH_KNOWN_HOSTS` | Chave publica conferida do servidor SSH |

Usar uma chave exclusiva de producao, separada da automacao dev e do acesso
pessoal. A chave publica fica no `authorized_keys` do root com a opcao
`restrict`, que bloqueia encaminhamentos e PTY. Os comandos de deploy
ainda executam como root. As credenciais da aplicacao continuam no servidor.

## Execucao

1. Push em `main` inicia **Deploy main - Hetzner (producao)**.
2. Testes e build precisam passar antes do acesso de deploy.
3. O script valida o perfil `prod` no Compose e no container existente.
4. A imagem ARM64 e enviada diretamente ao Docker do servidor, sem registry.
5. O script cria um backup PostgreSQL em formato custom e valida seu catalogo
   com `pg_restore --list`. Se o backup falhar, o app nao e atualizado.
6. Somente o servico `app` e recriado, com `--no-deps --no-build --pull never`.
7. A pagina `/login.xhtml` deve responder de dentro do novo container.

O banco, Caddy e volumes nao sao recriados. Flyway executa normalmente as
migracoes pendentes quando o app inicia. A recriacao do unico container causa
uma breve indisponibilidade em producao.

O backup fica em
`/opt/arenamatch/.deploy-main/backups/<data-UTC>-<commit>.dump`, com acesso
restrito pelo `umask 077`. E um backup local; nao substitui uma politica de
backup fora do servidor. Backups e imagens antigas sao preservados;
acompanhar o espaco em disco.

Se a atualizacao ou a verificacao HTTP falhar, o script tenta restaurar a imagem
anterior e marca a execucao como falha. **Migracoes de banco nao sao desfeitas
automaticamente**: a restauracao do banco exige avaliacao dos dados gravados
apos o backup. Novas migracoes precisam ser compativeis com a versao anterior.

O commit publicado fica em `.deploy-main/current-commit` e a imagem anterior
em `.deploy-main/previous-image`. Um lock local e a concorrencia do Actions
impedem dois deploys de producao simultaneos. Dev possui workflow, chave,
imagem e diretorio de estado separados.

## Validacao sem deploy

No servidor, a partir de uma copia do script:

```bash
bash scripts/deploy-main.sh /opt/arenamatch docker-compose.yml \
  arenamatch app db 0000000000000000000000000000000000000000 --check
```

Esse modo confere o alvo e as ferramentas de backup sem criar backups,
atualizar containers ou aplicar migracoes.

## Acompanhar e operar

No GitHub, abrir Actions -> **Deploy main - Hetzner (producao)** e conferir
a execucao da branch `main`. Verde indica que testes, build e deploy passaram.
Falhas aparecem em vermelho, com detalhes na etapa correspondente.

Para operar o Compose manualmente, incluir o override da imagem publicada:

```bash
cd /opt/arenamatch
docker compose -p arenamatch -f docker-compose.yml \
  -f .deploy-main/compose.override.yml ps
```

O disparo manual tambem exige a branch `main`. Reexecutar um workflow antigo
publica o commit daquela execucao; para publicar o estado atual, selecionar
`main` em Run workflow ou fazer novo push.
