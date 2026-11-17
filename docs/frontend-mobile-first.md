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
