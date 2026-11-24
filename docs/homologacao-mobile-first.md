# Homologação mobile first (ARENA-99)

Homologação do épico ARENA-87 com a aplicação real, em 17/11/2026, na branch `ARENA-99`, que acumula as histórias 88 a 98. A história 96 (ligas) foi adiada.
Convenções e checklist: `docs/frontend-mobile-first.md`.

## 1. Ambiente e método

- **Banco:** PostgreSQL 16 em contêiner próprio (`arenamatch-dev` para o app e `arena99_test` para os testes condicionais), com Flyway aplicado do zero.
- **Aplicação:** o jar da branch rodando na porta 8099. Ativação por e-mail e validação de CPF ficam desligadas, e o analytics também. **O SMTP aponta para um receptor local**, porque os `application*.properties` apontam para o Gmail real (ver item 5) e nenhum e-mail pode sair durante a homologação.
- **Navegador:**
  - Chrome headless controlado pelo protocolo DevTools, com um perfil por usuário.
  - Abaixo de 992px a emulação é de celular: toque, `deviceScaleFactor` 3 e `pointer: coarse`.
  - A digitação usa eventos reais de teclado.
  - Seleções do `p:selectOneMenu` são feitas abrindo o menu e tocando no item; o `selectValue()` do PrimeFaces 14 não dispara `change`.
- **Auditoria automática** em cada tela e largura:
  - rolagem horizontal da página;
  - elementos que vazam do viewport;
  - alvos interativos menores que 44px;
  - imagens sem `alt`;
  - quantidade de `h1`;
  - erros no console.
- **Larguras:** 320×640, 360×780, 390×844, 430×932, 768×1024, 1024×768, 1440×900 e 844×390 (celular na horizontal). As páginas públicas foram auditadas nas 7 primeiras.
- **Dados:**
  - Dois times cadastrados pela própria interface: A, mandante, com nome de 61 caracteres; B, visitante.
  - O administrador foi criado direto no banco; o login dele usou o código de acesso recebido no SMTP local.
  - Para testar placar, uma partida foi movida para uma data passada.

## 2. Fluxos de ponta a ponta

| # | Fluxo | Perfil e tela | Resultado |
|---|---|---|---|
| 1 | Cadastro completo (responsável, time com CEP, horários, termos) | A e B, 390px | Concluído; os dois times entram na busca |
| 2 | Regra violada no cadastro (horário de 1h para mandante) | Novo, 360px | Mensagem da regra, sem "400 Bad Request" |
| 3 | Login e busca por data e modalidade; desafio com mensagem | A, 390px | "Desafio enviado com sucesso" |
| 4 | Sino: convite recebido, "Negociar" abre o chat da partida | B, 390px | Painel com foco, sem rolagem lateral |
| 5 | Chat: B responde; A vê "não lidas", abre e lê | A e B, 390px | Indicador some após a leitura |
| 6 | Aceite do convite pelo sino; jogo na agenda do dia | B, 390px | "Desafio aceito! Jogo confirmado." |
| 7 | Placar: A informa 3x2, B confirma pela agenda | A e B, 390px | Confirmado; o dia continua aberto |
| 8 | Flyer do jogo | A, 390px | Imagem 1080×1350, diálogo de 366px, foco no diálogo |
| 9 | Pedido de cancelamento com motivo; B escolhe "Manter o jogo" | A 390px, B 360px | Motivo visível para B; o dia continua aberto |
| 10 | Ranking geral e meu scout após o placar | B, 360px | Abre em Futebol Society com A em 1º; scout com a derrota |
| 11 | Detalhes do adversário | B, 360px | Carrega sem erro |
| 12 | Fale conosco: envio, validação vazia, diálogo de exclusão (cancelar e confirmar) | A, 320px | Diálogo de 296px com foco em "Cancelar"; foco devolvido ao fechar |
| 13 | Admin: senha + código, busca, editar usuário, salvar parâmetro | Admin, 390px e 1440px | 3 usuários listados; foco no título da edição; mensagem na linha do parâmetro |

## 3. Matriz de cobertura

As 20 páginas `.xhtml` de `META-INF/resources`. "Telas" é o número de larguras auditadas sem nenhum problema.

| Página | História | Perfil / estado | Telas | Fluxos (item 2) |
|---|---|---|---|---|
| `index.xhtml` | 97 | Visitante | 7/7 | Links do menu e rodapé conferidos |
| `como-funciona.xhtml` | 97 | Visitante | 7/7 | Âncoras do guia |
| `encontrar-times-futebol-amador.xhtml` | 97 | Visitante | 7/7 | — |
| `privacidade.xhtml` | 97 | Visitante | 7/7 | — |
| `login.xhtml` | 90 | Visitante; admin na etapa do código | 7/7 | 3, 13 |
| `recuperar-senha.xhtml` | 90 | Visitante | 7/7 | Só renderização (envio de código não executado) |
| `ativar-conta.xhtml` | 90 | Visitante | 7/7 | Só renderização (ativação desligada no ambiente) |
| `cadastro.xhtml` | 91 | Novo cadastro; edição pelo time logado | 7/7 + 8/8 | 1, 2 |
| `busca-time.xhtml` | 92 | Time com resultados | 8/8 | 3 |
| `busca.xhtml` | 92 | Redireciona para `busca-time.xhtml` (não tem tela) | — | — |
| `minha-agenda.xhtml` | 93 | Convite, jogo marcado, placar pendente/aguardando/confirmado, pedido de cancelamento | 8/8 ¹ | 6 a 9 |
| `chat.xhtml` | 94 | Inbox, conversa, não lidas | 8/8 | 4, 5 |
| `ranking.xhtml` | 95 | Ranking com dados, scout com jogo | 8/8 | 10 |
| `adversario-detalhes.xhtml` | 95 | Adversário com histórico | 8/8 | 11 |
| `dashboard.xhtml` | 98 | Time em período de teste; administrador | 8/8 | — |
| `admin.xhtml` | 98 | Busca vazia e com resultado, edição, parâmetros | 8/8 | 13 |
| `fale-conosco.xhtml` | 98 | Formulário, erro, sucesso, exclusão pendente e confirmada | 8/8 | 12 |
| `minhas-ligas.xhtml` | 96 (adiada) | — | — | Fora do escopo |
| `explorar-ligas.xhtml` | 96 (adiada) | — | — | Fora do escopo |
| `gerenciar-liga.xhtml` | 96 (adiada) | — | — | Fora do escopo |

¹ Em 1024 e 1440px, as setas de período da agenda e o botão do calendário da busca têm 38px. É a exceção registrada em `tokens.css`: com mouse e tela larga, `--am-control-height` volta à densidade do tema. No toque, os dois têm 44px.

O layout autenticado (89) e o painel de notificações entram em todas as páginas logadas acima.

## 4. Defeitos encontrados e corrigidos na ARENA-99

| Defeito | Origem | Correção |
|---|---|---|
| Admin: "Editar" de um usuário quebrava a renderização; a lista mostrava 1 de 3 usuários e a edição não abria | ARENA-98 (`update` relativo dentro de `ui:repeat`, que é NamingContainer) | Ids absolutos no `update` (`admin.xhtml`) |
| Agenda: depois de qualquer ação (placar, aceite, cancelamento) o painel voltava ao primeiro dia do período | Anterior ao épico (`carregarCalendario` sempre selecionava o 1º dia) | Mantém o dia aberto quando ele continua no período (`MinhaAgendaBean`) + teste |
| Ranking e scout abriam em Futebol de Campo; time só de Society via ranking vazio e scout zerado | ARENA-67 (modalidade padrão fixa) | Abre na modalidade do time (`RankingBean`) + teste |
| CSS servido pelo JSF com cache de 7 dias e URL sem versão: após implantar, o navegador combinava HTML novo com CSS antigo | Anterior ao épico | `VersaoRecursoResourceHandler` acrescenta `v=` às bibliotecas `css`, `js` e `images` a cada subida + teste |
| Fale conosco: ao fechar o diálogo de exclusão, o foco se perdia | ARENA-98 (o `p:dialog` devolve o foco 100 ms depois; `assunto_focus` não existe no PF 14) | `supFocar` espera 150 ms e usa `assunto_label` |
| Busca: faixa escura vazia de 35px acima dos cartões | ARENA-92 (cabeçalho do `p:dataView`) | Cabeçalho escondido (`busca.css`) |
| Chat em tela cheia: a página de baixo transparecia | ARENA-94 (fundo com 97% de opacidade) | Fundo opaco no modo tela cheia (`chat.css`) |
| "x" das mensagens fecháveis com 32px | Tema; base ARENA-88 não cobria | 44px em `styles.css` |
| Início: barra do teste limitada a 15 dias, com teste de 90 | ARENA-98 | Barra retirada; dias restantes em texto |
| Cadastro mostrava "400 Bad Request: ..." | Anterior ao épico | Mostra o corpo da resposta (`CadastroBean`) + teste |
| Cadastro: as mensagens ficavam dentro da página (fim da etapa ou abaixo do botão) e às vezes era preciso rolar para vê-las; parecia que a tela tinha travado (teste do usuário) | ARENA-91 | Todas as mensagens vão para um aviso flutuante (`p:growl`), opaco, no topo no celular e no canto direito no desktop; o primeiro campo com erro vem para a tela |
| Cadastro, etapa Time: em "Mando de Campo" o rótulo caía para baixo do rádio, com espaço sobrando (teste do usuário) | ARENA-91 (CSS mirava em `td`, mas no PF 14 o `layout="pageDirection"` gera `div.ui-g > div.ui-g-12`) | Cada opção em linha flexível, rádio + rótulo de 44px (`cadastro.css`). Os rótulos `legend`/`span` da página ficaram com o mesmo tamanho dos `p:outputLabel` |
| Busca: no celular os cartões de resultado tinham larguras diferentes (o cartão com convite enviado, que tem dois botões lado a lado, saía mais largo) (teste do usuário) | ARENA-92 (o CSS mirava em `.ui-dataview-grid > div`, que não existe no PF 14; a grade nunca foi aplicada e cada cartão era um item flex do tamanho do conteúdo) | A grade passou para `.ui-dataview-content > .ui-dataview-row`, com o clearfix do `ui-g` desligado (`busca.css`). Conferido com 2 cartões (um com convite) em 320/390/490/768/1440px: mesma largura em todos |
| Login sem `h1`; textos da etapa do código sem acento | ARENA-90 / anterior | `h1` oculto visualmente; acentos no XHTML e no `LoginBean` |
| Testes de `analytics.js` quebrados pela ARENA-97 | ARENA-97 | DOM falso com `focus`/`contains` e 2 testes novos (19/19) |

## 5. Achados fora do escopo (não corrigidos)

- **Segredos versionados:** os `application*.properties` trazem a senha do SMTP do Gmail e a chave do Google Maps. Recomendação: trocar as duas credenciais e passar a lê-las de variáveis de ambiente.
- **Mensagens do backend sem acento** (ex.: "horarios", "nao"). Agora aparecem sem o prefixo HTTP, mas o texto continua vindo do backend.
- **Botões do painel de notificações** (`layout.xhtml`) usam `update` relativo dentro de `ui:repeat`, o mesmo padrão que quebrou o admin. Hoje funcionam (aceite e placar homologados), mas vale trocar por ids absolutos.
- **Busca:** o growl local continua sendo o único canal de retorno da tela (registrado na ARENA-92).
- **Ligas (ARENA-96):** adiadas; as três páginas não foram migradas nem homologadas.

## 6. Limitações desta rodada

- Emulação no Chrome (viewport, toque, densidade de pixels). Não substitui aparelho real; Safari/iOS e o WebView do app Android não foram testados.
- Leitor de tela não foi usado. Foco, `aria-*` e ordem do Tab foram conferidos por script.
- Zoom de 200%: equivale a reduzir o viewport pela metade (1440 → 720 CSS px, 1024 → 512), larguras cobertas pela auditoria. A ampliação só de texto não foi medida.

## 7. Testes automatizados

- `./mvnw clean test`: 83 testes, 0 falhas. Sem PostgreSQL, 7 são pulados; com `ARENA67_TEST_DB_*` apontando para o banco de teste, nenhum é pulado.
- `node --test src/test/js/analytics.test.cjs`: 19/19.
- XHTML alterados validados como XML.
