# Frontend mobile first — convenções (ARENA-87)

Base criada na ARENA-88. Vale para toda tela nova ou migrada do Arena Match (navegador e app Android via WebView).
Paleta e regras gerais de UI: `CLAUDE.md` (seções "Paleta de cores" e "Regras de layout e componentes").

## 1. Dependências e ordem de carregamento

| Recurso | Versão | Onde |
|---|---|---|
| Tema PrimeFaces | arya (PrimeFaces 14 via JoinFaces 5.3.4) | automático (`joinfaces.primefaces.theme`) |
| `css/tokens.css` | — | `<h:outputStylesheet library="css" name="tokens.css"/>` |
| `css/styles.css` | — | `<h:outputStylesheet library="css" name="styles.css"/>`, sempre depois de `tokens.css` |
| PrimeFlex | **3.3.1** (`primeflex.min.css`, unpkg) | `<link>` no head |
| Font Awesome | **6.4.0** (`all.min.css`, cdnjs) | `<link>` no head |

- Não carregar outra versão de PrimeFlex/Font Awesome nem outra biblioteca de CSS ou ícones.
- O tema arya entra antes dos recursos `h:outputStylesheet`; por isso as sobrescritas de `styles.css` vencem o tema com o escopo `.am-ui` (ver item 4) sem `!important`.
- Viewport padrão: `<meta name="viewport" content="width=device-width, initial-scale=1.0"/>`. **Não** usar `maximum-scale` nem `user-scalable=0`: o usuário precisa conseguir ampliar a tela. Os campos usam 16px, o que evita o zoom automático do iOS ao focar.
- O layout autenticado usa `viewport-fit=cover` (ARENA-89), com as áreas seguras tratadas pelos tokens `--am-safe-*`. Uma página que adotar `viewport-fit=cover` precisa aplicar esses tokens nos elementos fixos e nas bordas.
- O layout autenticado carrega ainda `css/layout.css` (depois de `styles.css`) e `js/layout.js`.

## 2. Breakpoints

O CSS fora de media query é o do **celular, a partir de 320px**. Telas maiores são ampliadas com `min-width`, nos mesmos pontos do PrimeFlex, para que `sm:`, `md:`, `lg:` e `xl:` dos utilitários e o CSS próprio mudem juntos:

| Nome | min-width | Uso típico |
|---|---|---|
| (base) | 0 | celular, 320–575px |
| `sm` | 576px | celular grande/horizontal: ações lado a lado, growl à direita |
| `md` | 768px | tablet: formulários em 2 colunas |
| `lg` | 992px | desktop: sidebar, 3 colunas, fundo fixo |
| `xl` | 1200px | desktop largo |

Larguras de validação: 320, 360, 390, 430, 768, 1024 e 1440 CSS px, mais uma orientação horizontal de celular.

Os breakpoints legados migram para essa escala na história de cada tela. O `991px` do antigo `styles.css` saiu na ARENA-88. O `980px`/`420px` do layout foi trocado por `lg` (992px) na ARENA-89.

### Exceções permitidas (fora do padrão min-width de largura)

| Consulta | Uso |
|---|---|
| `(hover: hover) and (pointer: fine)` | Só para **reduzir** densidade com mouse em telas largas (`--am-control-height` volta a 2.357rem a partir de `lg`) e para efeitos de hover. Nunca para esconder conteúdo. |
| `prefers-reduced-motion: reduce` | Desliga animações e transições (regra global em `styles.css`). |
| `max-height` / `orientation` | Ajustes de altura (ex.: sidebar em notebooks baixos, teclado aberto, celular na horizontal). Registrar o motivo no comentário da regra. |

## 3. Tokens (`css/tokens.css`)

Prefixo `--am-`. Cores seguem a paleta oficial; ao precisar de cor nova, reutilize um token.

- Cores: `--am-color-*` (superfície, texto, marca, estados), `--am-public-*` (páginas públicas), `--am-agenda-*` (legenda da agenda).
- Tipografia: `--am-font-size-{xs,sm,md,lg,xl,2xl}` (os maiores são fluidos com `clamp`), `--am-font-size-input` (≥16px).
- Espaço: `--am-space-{1..8}`, `--am-gutter` (16px no celular → 32px no desktop), `--am-container-{max,narrow,reading}`.
- Raios: `--am-radius-sm` 8px (botões), `-md` 12px (cards), `-lg` 16px (modais/cards grandes), `-pill`.
- Interação: `--am-touch-target` 44px, `--am-control-height`, `--am-focus-ring` (3px `#65d7ff`).
- Áreas seguras: `--am-safe-{top,right,bottom,left}`.

## 4. Componentes PrimeFaces — escopo `.am-ui`

Toda página que consome a base declara `<h:body styleClass="am-ui">` (hoje: `layout.xhtml`, `login.xhtml`, `privacidade.xhtml`). Dentro desse escopo:

- **Botões** (`.ui-button`): altura mínima de 44px no toque, rótulo que quebra linha em vez de vazar, botão só com ícone com largura mínima de 44px.
- **Campos** (`.ui-inputfield`, `.ui-selectonemenu`, `.ui-inputtextarea`): `max-width: 100%`, fonte de 16px, altura de 44px, foco em ciano e erro em vermelho. Itens das listas do selectOneMenu/autocomplete com 44px.
- **Diálogos**: nunca mais largos que a tela (`width` do `p:dialog` continua valendo até o limite); conteúdo rola dentro do diálogo; botão fechar com 44px; rodapé com ações que quebram linha.
- **Growl/mensagens**: growl ocupa a largura do celular e volta ao canto direito a partir de `sm`; textos longos quebram. O z-index continua com o PrimeFaces.

Páginas ainda não migradas que não têm `.am-ui` (ex.: `ativar-conta`, `recuperar-senha`) recebem a base na própria história (ARENA-90).

## 5. Classes compartilhadas (`css/styles.css`)

| Classe | Uso |
|---|---|
| `am-container` (`--narrow`, `--reading`) | Largura máxima + gutter lateral fluido |
| `am-stack` / `am-cluster` | Pilha vertical / grupo que quebra linha, com `gap` |
| `am-actions` (`--inline`, `--end`, `--between`) | Grupo de botões: empilhado e com largura total no celular, em linha a partir de `sm`. `--inline` mantém em linha também no celular (ações curtas) |
| `am-card` (`--elevated`, `--accent`), `am-card-title` | Superfície glass. `--accent` adiciona a faixa ciano no topo (login, termos) |
| `am-form-grid` (`--2`, `--3`), `am-field`, `am-field-label`, `am-field-hint`, `am-field-error`, `am-field--full` | Formulário de 1 coluna no celular, 2 a partir de `md`, 3 a partir de `lg` |
| `am-alert` (`--success`, `--warning`, `--danger`) | Mensagem de estado fixa na página. Texto claro; cor do estado na borda, fundo e ícone. Usar `role="status"` ou `role="alert"` |
| `am-empty`, `am-loading` | Estado vazio e carregamento |
| `am-chip` (`--success`, `--warning`, `--danger`, `--info`) | Etiquetas de status |
| `am-touch` | Área de toque de 44px para links/ícones próprios |
| `am-scroll-x` | Faixa com rolagem horizontal própria (abas, filtros, chips) |
| `am-text-break`, `am-truncate`, `am-clamp-2` | Conteúdo extenso: quebrar, cortar com reticências (exige acesso ao texto completo, ex.: `title` ou tela de detalhe), limitar a 2 linhas |
| `am-sr-only`, `am-skip-link` | Acessibilidade |

Utilitários legados mantidos para páginas ainda não migradas: `text-neon`, `bg-glass`, `rounded-btn`, `borda-liga-*`, `dot.liga`.
Removidas por não terem uso: `.navbar`, `.logo`, `.app-container`, `.sidebar-new`, `.content-new`, `.nav-item`, `.bottom-nav`.

## 5.1 Layout autenticado (`css/layout.css`, `js/layout.js`)

- **Abaixo de 992px:** coluna única. O cabeçalho rola junto com a página, para não ocupar altura fixa no celular, e a barra inferior fica fixa (Agenda, Buscar, Ranking, Chat, Dados, Suporte e Admin).
- **A partir de `lg`:** sidebar de 280px e cabeçalho fixo no topo.
- **Espaço da barra inferior:** a área de conteúdo já reserva `--am-bottom-nav-space` (altura da barra + safe area + folga), e o `html` tem `scroll-padding-bottom` com o mesmo valor. Páginas **não** devem somar `padding-bottom` próprio para a barra.
- **Teclado virtual:** enquanto um campo de texto está focado, o `js/layout.js` aplica `html.am-editando` e a barra inferior sai da frente do campo.
- **Barra inferior com zoom:** com zoom ou fonte ampliada, os itens mantêm 44px de largura e a barra passa a rolar na horizontal, então nenhum item some.
- **Painel de notificações:** o sino é um `<button>` com `aria-expanded`; o painel fecha com Esc ou com o botão fechar. O estado aberto é mantido quando `painelNotificacoesGlobais` é recriado por Ajax/WebSocket.
- **Exceções registradas:**
  - `(min-width: 992px) and (max-height: 760px)` compacta a sidebar.
  - `(orientation: landscape) and (max-height: 480px)` usa a barra inferior baixa, com ícone e rótulo lado a lado.
- **Camadas:** cabeçalho 90, sidebar 100, barra inferior 999. Diálogos e growl ficam a partir de 1000 (PrimeFaces). O banner de analytics usa 2000.

## 5.2 Páginas de acesso (`css/acesso.css`, `css/login-publico.css`)

Login, ativação e recuperação de senha (ARENA-90):

- **Cabeçalho compartilhado:** `WEB-INF/template/cabecalho-acesso.xhtml` traz logo, menu e "Saiba mais", e carrega `js/login-publico.js`.
  - **Abaixo de `lg`:** menu recolhido em botão de 44px. Ele fecha com Esc, ao tocar fora ou ao sair pelo Tab, e o cabeçalho rola com a página.
  - **A partir de `lg`:** menu em linha e cabeçalho fixo.
- **Cartão:** `.acesso-card` com `am-card am-card--elevated am-card--accent`. No celular começa no topo; a partir de `md` fica centralizado.
- **Mensagens:** erros e avisos vão em `p:messages` dentro do cartão, não em growl, para não cobrir o título nem o campo.
- **Ação principal:** `p:commandButton` com `.acesso-cta` (52px). Ser um botão de envio permite enviar o formulário pela tecla "Ir"/Enter do teclado.
- **Teclado do celular**, por atributos passthrough (`xmlns:pt`):
  - e-mail: `type="email"`, `autocomplete` e `autocapitalize="none"`;
  - código: `inputmode="numeric"` e `autocomplete="one-time-code"`;
  - senha: `autocomplete="current-password"` ou `"new-password"`.
- **Exceção registrada:** `(max-height: 520px)` compacta o topo do cartão (teclado aberto ou celular na horizontal).
- **Campos de senha:** a área de toque de 44px do mostrar/ocultar senha (`toggleMask`) vale para todas as páginas `.am-ui` (`styles.css`).

## 5.3 Cadastro e edição do time (`css/cadastro.css`, `js/cadastro.js`)

Assistente de três etapas (ARENA-91):

- **Estrutura:** um `am-card` com cabeçalho, corpo e rodapé. O cabeçalho traz o indicador de etapas: uma `ol` com `aria-current="step"`, cujos rótulos ficam em caixa normal no celular e em caixa alta a partir de 576px. O rodapé tem Voltar/Próximo, que dividem a largura no celular.
- **Visitante:** vê o mesmo cabeçalho público das páginas de acesso (`cabecalho-acesso.xhtml`) e um `h1` no cartão. O `.am-layout-login` deixou de centralizar verticalmente: o conteúdo começa no topo.
- **Usuário logado:** a página define `tituloPagina` ("Meus dados") para a barra superior.
- **Campos:** `am-form-grid am-form-grid--2` (uma coluna no celular, duas a partir de 768px), `am-field` e rótulos `p:outputLabel` ligados por `for`.
- **Teclado:** CPF, CEP e horários abrem o teclado numérico; o celular abre o de telefone e a taxa, o decimal. Os campos usam `autocomplete` quando há um valor padrão.
- **Mensagens perto da ação:**
  - Busca de CEP/GPS, upload do escudo e "Adicionar horário" exibem a resposta logo abaixo do próprio controle. Um `p:messages` local é renderizado só quando `param['jakarta.faces.source']` é o componente que disparou a ação, então a mensagem não se repete.
  - Navegação entre etapas, finalização e desativação usam as mensagens gerais (`formCadastro:msgs`) acima do rodapé. `js/cadastro.js` rola até elas quando a etapa não muda.
  - Quando a etapa muda, a página volta ao topo do cartão e o foco vai para o título da etapa.
- **Horários configurados:** lista em cartões (`ul`) no lugar da tabela com reflow. A lixeira tem 44px, fica sempre visível e tem `aria-label` com dia e horário (cenário do ARENA-32).
- **Dias da semana:** 4 colunas no celular e 7 a partir de 576px, todos com 44px.

## 5.4 Busca de adversários (`css/busca.css`)

`busca-time.xhtml` (ARENA-92):

- **Filtros:** começam em uma coluna. São 2 colunas a partir de 576px e, a partir de 1200px, uma faixa de 4 colunas com a distância e o Buscar na segunda linha.
  - **Ordem:** os campos obrigatórios (data e modalidade) vêm primeiro.
  - **Data:** o campo é só leitura (`readonlyInput`) e é escolhido pelo calendário, que não abre o teclado no celular.
  - **Busca:** Enter executa a busca (`p:defaultCommand`).
  - **Distância:** a alça do slider tem 28px.
- **Resultados:** o `p:dataView` usa o grid clássico `ui-g`. A grade vira CSS grid com uma coluna no celular e `repeat(auto-fill, minmax(19rem, 1fr))` a partir de 576px, no lugar das regras 50%/100% por `max-width`.
- **Contêiner `dataView`:** os updates continuam mirando `dataView`, que agora é um `h:panelGroup` com o estado vazio, a contagem de resultados e o `p:dataView` (`listaResultados`).
- **Paginação:** anterior, 3 páginas e próxima, com botões de 44px; a barra só aparece quando há mais de uma página.
- **Cartão:** identidade com o nome em até 3 linhas, distância, chips de modalidade e categoria, e uma lista de dados comparáveis (`dl`) com horário, mando, taxa e histórico.
  - Ações: convite enviado ou recebido em `am-alert`. Os botões ficam lado a lado quando cabem (mínimo de 9rem) e empilham no celular.
  - Revisão do desafio: adversário, modalidade, categoria e data, mensagem com rótulo, e Não/Enviar.
- **Mensagens:** a página mantém o `p:growl`. Como as ações acontecem dentro de cartões da lista, o growl fixo no topo continua visível em qualquer ponto da rolagem. O estado do convite também aparece no próprio cartão.
- **`busca.xhtml`:** entrada legada sem link no sistema, com filtros antigos e um "Desafiar" sem ação. Passou a redirecionar para `busca-time.xhtml` (`f:viewAction`).

## 5.5 Agenda (`css/agenda.css`, `js/agenda.js`)

`minha-agenda.xhtml` (ARENA-93):

- **Calendário:** 15 dias em 5 colunas x 3 linhas, em qualquer largura.
  - **Container query:** o calendário usa `container: agenda-calendario / inline-size`. Os rótulos dos dias (Jogo, Pendente, Placar...) aparecem quando o próprio calendário tem pelo menos 30rem.
  - **Abaixo disso:** no celular e na coluna estreita do desktop ficam as marcas coloridas. Os rótulos continuam no HTML, visualmente ocultos, para leitores de tela.
  - **Dia selecionado:** anel branco, distinto das cores de estado, e `aria-current`.
- **Desktop ≥1200px:** calendário fixo (`sticky`) à esquerda e detalhes do dia à direita.
- **Eventos:** cartão com status, título ou confronto, chips, `dl` de dados (horário, local, distância, taxa), recado e ações. A cor do estado fica na borda e no rótulo, via variáveis `--agenda-tom*`.
- **Placar:**
  - A entrada de gols serve tanto ao placar pendente quanto à disputa, com `p:outputLabel` ligado a cada campo.
  - O envio usa `process="@this placarEntrada"`. Os campos de todos os jogos do dia gravam na mesma propriedade do bean, e com `@form` um jogo vazio sobrescrevia os gols informados em outro.
- **Mensagens:** só o growl global (`:globalForm:msgs`). Várias ações atualizam `painelNotificacoesGlobais`, que envolve a página, e o growl próprio da página mostrava as mensagens em dobro.
  - Erros da carga inicial aparecem num `p:messages` renderizado apenas fora de postback.
- **Foco:** `agenda.js` devolve o foco ao mesmo elemento após cada AJAX. Se o elemento sumiu, ele vai para o dia selecionado ou para o título dos detalhes.
  - O painel "Todos meus jogos" é um `button` com `aria-expanded` e fecha com Esc.
- **Flyer:** os botões ficam no rodapé do diálogo, sempre visíveis, e a prévia (4:5) limita a altura à viewport.
  - "Compartilhar" aparece só quando `navigator.canShare({files})` aceita a imagem.
  - Ao fechar, o foco volta a "Gerar flyer".

## 5.6 Chat (`css/chat.css`, `js/chat.js`)

`chat.xhtml` (ARENA-94):

- **Celular (< 768px):** mostra a caixa de entrada **ou** a conversa.
  - **Tela cheia:** a conversa aberta é `position: fixed`, com topo e altura vindos do `visualViewport` (`--chat-vv-topo`, `--chat-vv-altura`, calculados por `chat.js`). Assim o campo e o botão de envio ficam acima do teclado, também com o teclado aberto e na horizontal. Sem JS, vale `100dvh`.
  - **Bloqueio de rolagem:** `html.chat-tela-cheia` impede a página de rolar por trás.
  - **Voltar:** botão de 44px ou Esc.
- **≥768px:** os dois painéis lado a lado, com altura da tela (desconta a barra inferior abaixo de 992px e o cabeçalho fixo acima).
- **Atualizações parciais:**
  - Abrir ou voltar recria o bloco `chat`.
  - Mensagem nova (WebSocket) e envio recriam só `chatCabecalho`, `chatMensagens` e a inbox. O campo de digitação não é recriado: o texto e o teclado continuam.
  - **Rascunho:** fica no `sessionStorage` por conversa. Quando `painelNotificacoesGlobais` recria a página, o rascunho e o foco voltam ao campo.
- **Rolagem:**
  - O contêiner de rolagem fica fora da área atualizada, então a posição de leitura se mantém.
  - Só desce sozinho se o leitor já estava no fim. Caso contrário, mostra o botão "Novas mensagens".
- **WebSocket:**
  - Uma única conexão, com inscrição na sala da conversa aberta, sincronizada pelos atributos `data-conversa-*`.
  - Reconecta com espera progressiva e, ao reconectar, ao voltar do segundo plano ou ao recuperar a rede, busca o que chegou nesse intervalo.
  - O estado da conexão aparece no cabeçalho (`role="status"`).
- **Envio:**
  - Enter envia só com teclado físico (`hover: hover` e `pointer: fine`). No celular, Enter quebra a linha.
  - Tocar no botão não tira o foco do campo.
  - O campo cresce até 8rem.
- **Segurança:** o texto das mensagens agora é escapado (antes `escape="false"`, um XSS armazenado). As quebras de linha aparecem por `white-space: pre-wrap`.

## 5.7 Desempenho (`css/desempenho.css`, `js/desempenho.js`)

`ranking.xhtml` e `adversario-detalhes.xhtml` (ARENA-95) compartilham o módulo `dsp-`:

- **Acesso:** sem acesso completo (`sessaoBean.acessoCompleto`: ADMIN, PRO/PREMIUM pago ou teste válido), a página mostra só o cartão `dsp-bloqueado`; modalidade e abas não aparecem.
- **Controles:** a modalidade e as abas ficam fora da área atualizada. A troca atualiza só `abas` e `painelDesempenho`, sinaliza carregamento com `aria-busy` e devolve o foco à aba.
- **Ranking:** tabela HTML semântica (`caption`, `th scope`, `abbr`) dentro de `am-table-scroll`.
  - Abaixo de 576px, Pos e Time ficam fixos (sticky), o escudo some e o nome quebra em linhas. As estatísticas rolam no próprio contêiner, com dica "Deslize a tabela" exibida só quando há colunas escondidas.
  - O time do usuário é destacado ("Seu time"). Uma legenda (`details`) explica as abreviações.
- **Métricas:** `dl.dsp-metricas` com 4 colunas no celular (3 para o confronto) e todas em uma linha a partir de 768px (8 métricas a partir de 1200px).
- **Jogos:** cartões com resultado V/E/D (texto completo para leitor de tela), data e "placar contra adversário". "Carregar mais" leva o foco ao primeiro jogo novo e anuncia a quantidade.
- **Erro de carga:** `RankingBean.erroCarga` mostra `am-alert--danger` com "Tentar novamente" em vez de quebrar a página.
- **Adversário:** sem growl próprio (usa o global). Desafio com rótulo no campo, ação principal no alcance do polegar e foco gerenciado ao abrir, cancelar e enviar. A partir de 1200px, confronto e resultados ficam lado a lado.

## 5.8 Páginas públicas (`css/institucional.css`, `guia-publico.css`, `amistosos.css`, `analytics.css`)

`index`, `como-funciona`, `encontrar-times-futebol-amador` e `privacidade` (ARENA-97) têm CSS próprio, sem PrimeFaces e sem os tokens `--am-*`, mas com os mesmos breakpoints (576/768/992/1200) e escrito mobile first:

- **Cabeçalho:** abaixo de 992px o menu fica recolhido (`js/menu-publico.js`), que fecha com Esc, ao escolher um link, ao tocar fora, ao sair com Tab e ao ir para o desktop. Sem JS, os links quebram em linhas abaixo da marca.
- **Âncoras:** `menu-publico.js` mede o cabeçalho e publica `--site-header-h`; o `scroll-padding-top` e os índices laterais (sticky) usam esse valor, em vez de alturas fixas por breakpoint.
- **Safe area:** `viewport-fit=cover`, com as laterais do contêiner, o menu, o rodapé e o banner de consentimento respeitando `env(safe-area-inset-*)`.
- **Grades:** funcionalidades e planos em 1 coluna, 2 a partir de 768px (o terceiro cartão ocupa a linha) e 3 a partir de 992px. Guia e amistosos com índice acima do texto no celular e lateral a partir de 992px.
- **Toque:** links de texto, itens do FAQ, rodapé e índices com 44px; textos de apoio com no mínimo 11–12px.
- **Privacidade:** usa o cabeçalho e o rodapé públicos (antes era uma página isolada com PrimeFlex e `<style>`). O texto legal não mudou.
- **Consentimento:** painel na base da tela no celular e cartão no canto a partir de 576px; rola por dentro em telas baixas. Após a escolha, o foco vai para "Cookies de estatísticas" no rodapé; reaberto, Esc fecha mantendo a escolha.
- **Capturas do guia:** cada imagem é um link para o arquivo em tamanho real ("Ampliar"). As capturas ainda são anteriores às telas migradas e precisam ser refeitas com o app em execução.

## 6. Tabelas

1. **Padrão: cartões ou reflow** quando cada linha é lida sozinha (agenda, notificações, convites).
2. **Rolagem em contêiner próprio** quando é preciso comparar colunas (ranking, admin): envolver em
   `<div class="am-table-scroll" tabindex="0" role="region" aria-label="...">`. A página não rola na horizontal; o contêiner mostra sombras nas bordas enquanto há colunas escondidas. A largura mínima da tabela é ajustável por `style="--am-table-min-width: 640px"` ou classe da página.

## 7. Extração de estilos por página

Ao migrar uma tela (ARENA-89 a 98):

1. Mover o bloco `<style>` e os `style="..."` da página para `css/<modulo>.css` com prefixo do módulo (ex.: `css/agenda.css` com `agenda-*`), carregado por `h:outputStylesheet` depois de `styles.css`.
2. Reescrever as regras como mobile first (base para celular, `min-width` para ampliar); trocar cores e espaços fixos por tokens.
3. Reaproveitar `am-*` antes de criar classe nova. Se um padrão aparecer em duas telas, ele sobe para `styles.css`.
4. Manter `style` inline só para valores dinâmicos vindos do EL (ex.: cor do time).
5. Conferir estados de carregamento, vazio, erro e sucesso, e as atualizações Ajax (`update`) que dependem de ids.

## 8. Checklist de validação por entrega

- [ ] Sem rolagem horizontal da página em 320, 360, 390/430, 768, 1024 e 1440px e em um celular na horizontal.
- [ ] Botões, links de ação e campos com área de toque ≥ 44px no celular.
- [ ] Tab percorre a tela em ordem lógica, com foco visível; diálogos podem ser fechados pelo teclado.
- [ ] Zoom do navegador em 200% e texto ampliado sem perder conteúdo ou ações.
- [ ] Nomes longos (time, liga, campo, endereço) quebram ou têm acesso ao texto completo.
- [ ] Estados desabilitado, erro e sucesso visíveis sem depender só de cor.
- [ ] Mensagens Ajax (growl/messages) aparecem e não cobrem a ação focada.
- [ ] Antes/depois registrado no Jira da história.
