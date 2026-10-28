# ARENA-62 — Guia público e compatibilidade do WebView

## Comparação com ARENA-61

A branch ARENA-62 foi criada diretamente da main (`0cb6a60`). Os arquivos públicos
da ARENA-61 foram reaproveitados como base visual, sem alterar aquela branch.
Além da apresentação, esta versão inclui guia ilustrado, resumo em três passos,
FAQ compartilhado, textos de planos alinhados às limitações atuais e configuração
para ativar futuramente a apresentação na raiz.

## Rotas e entrada padrão

- `/`: redireciona ao login por padrão.
- `/login.xhtml`: autenticação existente, com atalhos de apresentação e guia.
- `/index.xhtml`: apresentação pública.
- `/como-funciona.xhtml`: guia público, sem necessidade de cadastro ou sessão.

Não há mudança em cookies, sessão, autenticação ou permissões do domínio.
Os templates públicos são separados do layout autenticado.

## Ativação posterior da apresentação na raiz

Manter nesta entrega:

```properties
arenamatch.pagina-publica-inicial-habilitada=false
```

Quando houver uma decisão explícita de ativação, definir a propriedade como `true`
na configuração do ambiente e reiniciar/reimplantar a aplicação. Também é possível
usar a variável `ARENAMATCH_PAGINA_PUBLICA_INICIAL_HABILITADA=true` ou o argumento
`--arenamatch.pagina-publica-inicial-habilitada=true`.

Com `true`, `/` passa a redirecionar para `/index.xhtml`; a URL do login continua
funcionando. Para reverter, definir `false` e reiniciar/reimplantar.

O Flutter local em `C:\desenvolvimento\repository\android\arenamatch\lib\main.dart`
já carrega `https://www.arenamatch.app.br/login.xhtml`. Não houve alteração Android
nesta entrega. Essa constatação não comprova a URL usada por versões já instaladas.

**Antes de ativar:** confirmar qual versão foi publicada na Play Store e avaliar
a base ainda instalada. Aplicativos antigos que abrem `/` passarão a mostrar a
apresentação, mesmo que uma atualização mais nova já esteja publicada. Definir uma
estratégia para essas instalações ou manter a configuração desabilitada. Não há
detecção por User-Agent nem atualização forçada implementada aqui.

## Conteúdo e manutenção

O guia utiliza capturas reais em ambiente isolado e dados demonstrativos.
Não há depoimentos, métricas ou avaliações inventadas. As imagens de cadastro,
busca e convite devem ser atualizadas se o fluxo do produto mudar.

Os textos não fixam quantidades para raio do Básico, intervalo entre agendamentos,
antecedência ou duração do teste: essas regras continuam parametrizadas no sistema.
Gestão de elenco e campeonatos são identificados como recursos futuros. Os preços
PRO/PREMIUM são apresentados como valores planejados; botões não cobram nem ativam
assinaturas. A política de privacidade existente é vinculada sem inventar novas
finalidades de coleta.

## Verificação

Executar `mvn clean test`. Conferir raiz com propriedade ausente, false e true,
acesso anônimo ao guia/apresentação e proteção das páginas internas. Revisar desktop,
320 px e 430 px, índice do guia, links de cadastro/login, FAQ e menu por teclado.
Para comparar versões, usar checkouts separados ou alternar branches com o workspace
limpo; nenhuma mesclagem da ARENA-61 na main foi feita por esta implementação.

### Resultado da implementação

- `mvn clean test` e conferência final com `mvn test`: 33 testes, sem falhas ou erros.
- Seis testes cobrem acesso público, páginas protegidas e configuração da raiz
  ausente, desabilitada e habilitada.
- Apresentação e guia retornam HTTP 200 sem autenticação; raiz e página interna
  preservam o redirecionamento ao login.
- XHTML validado e `git diff --check` sem erros.
- Revisão visual em desktop, 800, 900, 320 e 430 px; sem rolagem horizontal nas
  páginas públicas. Conferidos carregamento das cinco capturas, índice do guia,
  FAQ, atalhos de login/cadastro e menu do login por Enter/Escape.
- As capturas foram feitas com banco temporário isolado e dados demonstrativos;
  nenhum convite foi enviado. O ambiente de captura não integra a aplicação.

As alterações permanecem locais na branch ARENA-62 para revisão, sem publicação.
