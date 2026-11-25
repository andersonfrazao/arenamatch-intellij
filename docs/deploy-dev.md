# Deploy automatico de dev na Hetzner

Fluxo: push na branch `dev` -> testes Java/JavaScript -> imagem Docker Java 21
identificada pelo SHA do commit -> envio por SSH para `178.105.94.89` ->
atualizacao somente do servico de aplicacao dev no Docker Compose existente.

A pipeline esta em `.github/workflows/deploy-dev.yml`. O script remoto esta em
`scripts/deploy-dev.sh`. Nao ha dependencia de registry nem de Git no servidor.
O workflow manual tambem exige que a branch selecionada seja `dev`.

## 1. Identificar o ambiente existente

Antes da primeira execucao, confirmar no servidor:

```bash
docker compose ls
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
```

Na pasta do dev, identificar o arquivo Compose, o nome do projeto e o servico
da aplicacao. Para um container conhecido:

```bash
docker inspect NOME_CONTAINER_DEV --format '{{ index .Config.Labels "com.docker.compose.project" }}'
docker inspect NOME_CONTAINER_DEV --format '{{ index .Config.Labels "com.docker.compose.service" }}'
docker inspect NOME_CONTAINER_DEV --format '{{ index .Config.Labels "com.docker.compose.project.config_files" }}'
```

O Compose alvo deve declarar `SPRING_PROFILES_ACTIVE: dev` no servico da
aplicacao. O container existente tambem deve estar nesse perfil. A pipeline
recusa o deploy se essas condicoes nao forem satisfeitas, ou se nao encontrar
exatamente um container para o projeto/servico informado.

**O `docker-compose.yml` da raiz usa `prod` e nao serve como alvo desta pipeline.**
Se ainda nao existir um ambiente dev, primeiro provisionar um Compose proprio,
com banco, volume de uploads e porta/dominio separados da producao. Confirmar
que o datasource do dev aponta para o banco dev antes de habilitar os pushes.
Esta pipeline atualiza um dev existente; nao provisiona infraestrutura.

## 2. Preparar o acesso SSH

O servidor confirmado e Linux **ARM64/aarch64**, com Bash, Docker Engine, Docker Compose
v2, Python 3 e `flock`. O usuario SSH precisa acessar Docker sem `sudo` interativo
e escrever na pasta dev. O servico de banco dev ja deve estar funcionando.

Criar uma chave exclusiva para o GitHub Actions na sua maquina:

```bash
ssh-keygen -t ed25519 -C "github-actions-arenamatch-dev" -f ./arenamatch-dev-actions
```

Deixar a passphrase vazia para essa chave de automacao. Adicionar o conteudo de
`arenamatch-dev-actions.pub` ao `~/.ssh/authorized_keys` do usuario no servidor.
Nao commitar nem compartilhar a chave privada. O acesso ao grupo `docker` permite
administrar o host; usar um usuario exclusivo para facilitar revogacao e auditoria.

Capturar a chave publica do servidor e conferir sua fingerprint por um canal
confiavel, por exemplo o console Hetzner:

```bash
ssh-keyscan -p 22 -H 178.105.94.89 > known_hosts_dev
ssh-keygen -lf known_hosts_dev
# No console do servidor:
ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

Copiar para o secret a linha do `known_hosts_dev` cuja fingerprint foi conferida.
Se a porta SSH for diferente, ajustar `ssh-keyscan` e a variavel da pipeline.

## 3. Configurar o GitHub

Em Settings -> Secrets and variables -> Actions, cadastrar **Repository secrets**:

| Secret | Conteudo |
| --- | --- |
| `DEV_SSH_PRIVATE_KEY` | Chave privada exclusiva, incluindo cabecalho e rodape |
| `DEV_SSH_KNOWN_HOSTS` | Linha de host key conferida no passo anterior |

Os valores abaixo foram confirmados no servidor e sao os padroes da pipeline.
Na aba Variables, podem ser sobrescritos com **Repository variables**, se necessario:

| Variavel | Valor |
| --- | --- |
| `DEV_SSH_HOST` | Opcional; padrao `178.105.94.89` |
| `DEV_SSH_PORT` | Opcional; padrao `22` |
| `DEV_SSH_USER` | `root` |
| `DEV_DEPLOY_PATH` | `/opt/arenamatch-dev` |
| `DEV_COMPOSE_FILE` | `docker-compose.yml` |
| `DEV_COMPOSE_PROJECT` | `arenamatch-dev` |
| `DEV_COMPOSE_SERVICE` | `app-dev` |

O arquivo deve reunir a configuracao do dev; setups que dependem de
varios `-f`, `--env-file` ou Compose profiles precisam de ajuste antes de ativar.
As variaveis de ambiente usadas pelo Compose devem estar no `.env` da pasta dev.
Os caminhos e nomes configurados devem ser simples, sem espacos.

Credenciais de banco, SMTP e JWT continuam na configuracao do servidor. Ha
credenciais ja versionadas nos arquivos atuais do projeto; planejar sua migracao
para variaveis de ambiente e a rotacao das credenciais expostas.

## 4. Primeira execucao

Integrar estes arquivos na branch `dev` e fazer push. Acompanhar o workflow
**Deploy dev - Hetzner** em Actions. Para repetir, selecionar Run workflow com
a branch `dev` (o botao manual pode depender de o workflow tambem estar presente
na branch padrao do repositorio).

O runner executa `mvn clean verify` com PostgreSQL 15 descartavel, habilitando
os testes de integracao ARENA-67, e executa os testes JavaScript.
Somente depois dos testes e do build envia a imagem `arenamatch-dev:<SHA>`.
O build usa QEMU/Buildx com `--platform linux/arm64`, compativel com a Hetzner atual.

O script preserva a configuracao existente e gera apenas
`.deploy-dev/compose.override.yml` com a imagem a usar. Executa `up` somente
no servico informado, com `--no-deps --no-build --pull never`. O banco nao e
recriado e os volumes existentes continuam vinculados. Flyway ainda executa as
migracoes pendentes normalmente durante a inicializacao da aplicacao.

A verificacao faz HTTP em `http://127.0.0.1:8080/login.xhtml` de dentro do
container usando o `wget` da imagem Alpine. E uma verificacao da pagina publica,
nao um teste completo dos fluxos de negocio. A aplicacao deve manter essa porta
interna e essa pagina. O proxy/HTTPS externo nao e validado por essa verificacao.

Se a atualizacao ou a verificacao falhar, o script tenta recriar o app com a imagem
anterior e retorna erro para o Actions. Isso **nao desfaz migracoes de banco**;
migracoes devem continuar compativeis com a versao anterior. Ha uma breve
indisponibilidade durante a recriacao do unico container dev.

O ultimo commit publicado fica em `.deploy-dev/current-commit` e a imagem anterior
em `.deploy-dev/previous-image`. Deploys sao serializados pelo Actions e por um
lock no servidor. Imagens antigas permanecem no host para permitir restauracao;
acompanhar o espaco em disco ao longo do uso.

## Operacao manual depois de habilitar a pipeline

Ao operar o dev manualmente, incluir o override para preservar a imagem publicada:

```bash
docker compose -p PROJETO_DEV -f ARQUIVO_COMPOSE_DEV \
  -f .deploy-dev/compose.override.yml ps
```

Reexecutar um workflow antigo publica o commit daquela execucao. Para publicar
o estado atual, selecionar a branch `dev` no disparo manual ou fazer novo push.

Referencias: [GitHub Actions: deployments](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/control-deployments)
e [Docker Compose: up](https://docs.docker.com/reference/cli/docker/compose/up/),
[Docker: builds para outras arquiteturas](https://docs.docker.com/build/ci/github-actions/multi-platform/).

## Configuracao validada em 13/11/2026

- SSH existente validado como `root@178.105.94.89`, porta `22`.
- Projeto `arenamatch-dev`, servico `app-dev`, configuracao em
  `/opt/arenamatch-dev/docker-compose.yml`.
- Container `arenamatch-app-dev`, porta publicada `8081`, perfil Spring `dev`.
- Banco dev: `jdbc:postgresql://db-dev:5432/arenamatch-dev`.
- Uploads: `/opt/arenamatch-dev/uploads-dev` montado em `/app/uploads`.
- Java 21 ARM64, Compose 2.40.3, Python 3 e `flock` disponiveis.
- Pagina `/login.xhtml` respondeu dentro do container.
- `deploy-dev.sh ... --check` validou o alvo sem recriar containers.
- Chave exclusiva `github-actions-arenamatch-dev` criada e testada por SSH,
  com a opcao `restrict` no `authorized_keys` do root. Essa opcao bloqueia
  encaminhamentos e PTY; os comandos de deploy ainda tem privilegios de root.
- Secrets `DEV_SSH_PRIVATE_KEY` e `DEV_SSH_KNOWN_HOSTS` cadastrados no GitHub
  Actions. A chave privada temporaria foi apagada do servidor depois do teste.
  A chave privada compartilhada na conversa nao foi usada para a automacao;
  substituir/revogar essa chave pessoal apos garantir outro acesso administrativo.

Fingerprint ED25519 do host obtida pela conexao SSH existente:
`SHA256:bE1ne3B8XGEyTMD/J1LnpB6oSOsKTfSB2pqTGKKgwXY`.

Para repetir a validacao sem atualizar a aplicacao:

```bash
bash scripts/deploy-dev.sh /opt/arenamatch-dev docker-compose.yml \
  arenamatch-dev app-dev 0000000000000000000000000000000000000000 --check
```
