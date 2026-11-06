# Convenção de migrações Flyway

A partir do ARENA-67, novas migrações ainda não aplicadas em ambientes permanentes
usam o número do Jira e uma sequência interna, iniciando em zero:

```text
V67.0__ARENA_67_adicionar_modalidades_agenda_partida.sql
V67.1__ARENA_67_descricao_da_proxima_alteracao.sql
V68.0__ARENA_68_descricao_da_alteracao.sql
```

- A versão é numérica (`67.0`). Não usar letras na versão, como `V_ARENA_67.0`.
- Separar a versão da descrição com dois underscores (`__`).
- Cada versão deve ser única no projeto. Duas migrações do mesmo Jira não podem
  usar a mesma sequência, mesmo que tenham descrições diferentes.
- Uma migração já aplicada em DEV, homologação ou produção mantém nome e conteúdo.
  Novas alterações entram em um novo arquivo, com a próxima sequência do Jira.
- Não renomear migrações antigas para uniformizar o histórico.
- O projeto já permite execução fora de ordem (`spring.flyway.out-of-order=true`).
  Essa configuração não resolve dependências: antes de integrar branches, conferir
  se cada migração funciona na ordem numérica e se seus pré-requisitos estão presentes.

## Transição do ARENA-67

A migração de modalidades foi renomeada de
`V40__adicionar_modalidades_agenda_partida.sql` para
`V67.0__ARENA_67_adicionar_modalidades_agenda_partida.sql`, sem alteração do SQL.
O usuário confirmou que a versão anterior executou somente em bancos descartáveis
de teste. Não houve alteração do histórico de bancos permanentes.

Usar `mvn clean test` após a renomeação para remover a cópia antiga em `target/`.
Para os testes de integração, usar um PostgreSQL descartável novo e configurar
`ARENA67_TEST_DB_URL`, `ARENA67_TEST_DB_USER` (padrão `postgres`) e
`ARENA67_TEST_DB_PASSWORD`. Não reutilizar o banco de testes que já executou a V40:
o Flyway trataria V67.0 como outra migração e tentaria reaplicar o SQL.

O teste de migração prepara dados anteriores à mudança, executa a atualização,
confere o registro da V67.0 no histórico e verifica a preservação dos dados.

Validação realizada em 06/11/2026: `mvn clean test`, com PostgreSQL descartável
novo, concluiu 54 testes sem falhas, erros ou testes ignorados. O conteúdo SQL
manteve o mesmo SHA-256 e somente a V67.0 ficou nos recursos compilados.
