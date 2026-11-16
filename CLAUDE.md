# Arena Match — guia para o Claude

App web para times de futebol amador encontrarem adversários e organizarem amistosos. Também roda no app Android (Flutter) via **WebView**.

**Stack:** Java 21 · Spring Boot 3.5.9 · JSF/PrimeFaces via JoinFaces 5.3.4 (tema Arya) · PostgreSQL 15 + Flyway · Spring Security + JWT (HS256) · WebSocket STOMP/SockJS · Lombok · JUnit 5/Mockito.
Telas em `src/main/resources/META-INF/resources/*.xhtml`, CSS em `.../resources/css/`, template privado em `WEB-INF/template/layout.xhtml`.

> Referência técnica detalhada (pacotes, regras de negócio, tabelas, configuração, build e deploy): `docs/referencia-tecnica.md`. Este arquivo traz as **regras** que não podem ser quebradas.

## Arquitetura em camadas — REGRA OBRIGATÓRIA

Spring Boot e JSF rodam no **mesmo container**, mas o front (JSF) e o back (API REST) são separados internamente, **como se fossem aplicações distintas**. O objetivo é poder trocar o front no futuro (ex.: outro framework ou app nativo) sem refatorar regras de negócio. **Esta regra não pode ser quebrada.**

```
XHTML  →  Bean  →  Client  ──HTTP/REST + JWT──▶  Controller  →  Service  →  Repository  →  PostgreSQL
└──────── front (br.com.arenamatch.jsf) ────────┘                  └──────────── back (API) ────────────┘
```

| Camada | Pacote | Pode chamar | Responsabilidade |
|---|---|---|---|
| Página | `.xhtml` | **somente** Beans (EL `#{bean...}`) | Apresentação |
| Bean | `jsf.beans` (`@Named` + `@ViewScoped`/`@SessionScoped`) | **somente** Clients | Estado da tela, mensagens (`FacesContext`), navegação |
| Client | `jsf.client` (`@Component`) | **somente** a API via `RestClient` | Traduzir chamada Java ↔ HTTP, usando DTOs |
| Controller | `controller` (`@RestController`, `/api/...`) | **somente** Services | Expor endpoints REST, receber/devolver DTOs |
| Service | `service` | Repositories, outros Services, `integracao` | **Todas** as regras de negócio, validações e autorização |
| Repository | `repository` | Banco | Acesso a dados (Spring Data JPA) |

### O que é proibido
- XHTML chamar Client, Service ou qualquer coisa que não seja Bean.
- Bean injetar ou importar **Service, Repository, Controller ou Entity**. Bean só conhece Client e DTO.
- Bean ou Client conter regra de negócio (validação de domínio, cálculo, permissão). No Bean, só validação de usabilidade; a validação real é repetida no Service.
- Controller acessar Repository ou conter regra de negócio — ele só delega ao Service.
- Controller receber ou devolver **Entity**. Na fronteira HTTP trafegam apenas **DTOs** (`dto`).
- Service depender de `jsf` ou `controller` (nada de `FacesContext` no back).
- "Atalhos" por estarem no mesmo container: nunca injetar um Service num Bean "porque é mais rápido".

### Como funciona a comunicação
- `config/AppConfig` cria o `RestClient` com `arenamatch.api.base-url` e um interceptor que pega o `jwtToken` da sessão JSF e envia como `Authorization: Bearer`.
- O back valida o JWT (`JwtAuthenticationFilter`/`SecurityConfig`). Endpoints públicos (ex.: cadastro) são liberados **um a um** no `SecurityConfig`, por método e caminho exatos — nunca com curinga amplo.
- Erros da API chegam ao Client como exceção do `RestClient`; o Bean trata e mostra mensagem amigável, sem expor detalhes técnicos. No back, `exception/GlobalExceptionHandler` padroniza as respostas.
- Integrações externas (ViaCEP, geolocalização) ficam em `integracao` e só são chamadas por Services.

### Ao criar uma funcionalidade nova
Crie as camadas na ordem: migration Flyway → Entity → Repository → DTO → Service (+ testes) → Controller → Client → Bean → XHTML. O caminho em `@RequestMapping` do Controller precisa bater com a URI usada no Client.

- **Identidade e propriedade no backend:** o Service descobre o time pelo usuário autenticado (JWT), como em `PartidaExternaService.buscarTimeAutenticado()`. Em código novo, não confie em `timeId` vindo do cliente para decidir de quem é o dado.
- **Tempo real:** o Service persiste e depois publica com `SimpMessagingTemplate` em `/topic/...`; o envio de dados continua passando pela API REST.

## Planos de assinatura — REGRA DE NEGÓCIO

O plano pertence ao `Usuario` (`planoAssinatura` + `statusPagamento` + `dataExpiracao`). **Toda decisão de acesso passa por `AssinaturaService`** (`temAcessoCompleto` / `validarAcessoCompleto`), sempre no backend.

| Plano | Acesso |
|---|---|
| **Básico** (gratuito) | Agenda, busca, desafios, chat, registro manual de jogos. **Limites:** raio de busca máximo (`RAIO_MAXIMO_BUSCA_PLANO_BASICO_KM`, padrão 10 km) e intervalo entre desafios (`DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO`, padrão 15 dias; `0` desliga). **Sem** Ranking, Meu Scout e histórico de confrontos. |
| **Trial** | Acesso completo enquanto `TRIAL/TRIAL` e não expirado (`DIAS_TRIAL`, padrão 90). Ao expirar, vira Básico + `EXPIRADO` (tarefa agendada). |
| **PRO** | Acesso completo somente com `statusPagamento = PAGO`. |
| **PREMIUM** | Existe no enum e inclui PRO (`incluiPro()`), também exigindo `PAGO`. Campeonatos ainda não estão na `main`. |

- Limites e prazos vêm de `parametro_sistema` (via `ParametroSistemaService`). **Nunca** fixe esses números no código.
- Recurso pago novo: validar no Service com `AssinaturaService`; na tela, o Bean só mostra o estado bloqueado e a chamada para o PRO (não esconder a funcionalidade).
- Downgrade **não apaga dados**: só bloqueia o acesso até a reativação.
- Pagamento recorrente (Mercado Pago, ARENA-60) ainda não existe; não prometa compra ou ativação automática na interface.

## Banco de dados e Flyway

- Migrations em `src/main/resources/db/migration`, no padrão **`V<jira>.<seq>__ARENA_<jira>_<descricao>.sql`** (ex.: `V70.1__ARENA_70_placar_partida_externa.sql`). Detalhes em `docs/migracoes-flyway.md`.
- **Nunca altere nem renomeie uma migration já aplicada** em DEV/homologação/produção: crie a próxima sequência.
- Enums são gravados como texto (`EnumType.STRING`). Não renomeie valores persistidos. `CategoriaDTO` ainda aceita o **ordinal**, então não reordene `Categoria`.
- Dinheiro em código novo usa `BigDecimal` (`NUMERIC(12,2)`). `Time.valorTaxa` é `Double` legado; converta ao ler.
- A modalidade de uma partida é imutável (trigger no banco). O ranking oficial vem só de `partida` com placar `CONFIRMADO`; jogos registrados manualmente (`partida_externa`) **nunca** pontuam no ranking oficial.
- Testes de repositório usam PostgreSQL real e descartável, habilitados por variável de ambiente (`ARENA67_TEST_DB_*`, `ARENA70_TEST_DB_*`). Suba um container temporário (ex.: `postgres:15` em porta livre). Nunca aponte para o banco de desenvolvimento ou de produção.

## Framework de UI e CSS

- **PrimeFaces** com tema **Arya** (escuro) — `joinfaces.primefaces.theme=arya`. Use componentes `p:` antes de criar HTML/CSS próprio.
- **PrimeFlex 3.3.1** para layout e utilitários (`grid`, `col-12 md:col-6`, `flex`, `gap-3`, `p-3`...). É o "framework de CSS" do projeto — não adicionar Bootstrap, Tailwind ou similares.
- **Font Awesome 6.4.0** para ícones (`<i class="fas fa-...">`). Não misturar com outra biblioteca de ícones.
- CSS próprio: classes com prefixo `am-` (ex.: `am-sidebar`, `am-plan-card`). Estilo global em `css/styles.css`; páginas públicas em `css/institucional.css`.
- Evite `style="..."` inline e `!important` em código novo; prefira classe em arquivo CSS.

## Paleta de cores

Tema sempre **escuro**. Não criar telas com fundo claro.

### Marca
| Token | Hex | Uso |
|---|---|---|
| Ciano primário (app) | `#0ea5e9` | Ação principal, links, item de menu ativo, `.text-neon` |
| Ciano claro | `#38bdf8` | Ícones e texto destacado sobre fundo escuro, hover |
| Ciano marca (público) | `#19b5ee` | `--blue` das páginas públicas, logo "ARENA", materiais da Play Store |
| Ciano hover (público) | `#64d3fa` | Hover do botão primário público |

Logo: "ARENA" em ciano, "MATCH" em branco.

### Fundos e superfícies
| Token | Hex / valor | Uso |
|---|---|---|
| Fundo base | `#0b1220` | `--bg` das páginas públicas, fundo geral |
| Navy | `#0f172a` / `rgba(15,23,42,.95)` | Sidebar, navbar, bottom nav |
| Painel | `#111d30` | `--panel`, cards públicos |
| Vidro | `rgba(30,41,59,.7)` + `backdrop-filter: blur(10px)` | `.bg-glass`, cards do app |
| Borda | `rgba(255,255,255,.08–.1)` / `#25364c` | Bordas sutis |
| Borda forte | `#334155` / `#42617e` | Inputs, botão outline |

### Texto
| Token | Hex | Uso |
|---|---|---|
| Texto principal | `#e2e8f0` / `#f3f6fc` | Corpo |
| Branco | `#ffffff` | Títulos, ênfase |
| Texto secundário | `#94a3b8` / `#a8b6cc` | Legendas, menu inativo, `p` |
| Texto apagado | `#64748b` | Desabilitado, metadados |

### Status (semânticas)
| Significado | Hex | Onde aparece |
|---|---|---|
| Sucesso / confirmado / PRO | `#22c55e`, `#86efac`, `#25d366` | Jogo confirmado, plano PRO |
| Atenção / pendente / trial | `#ffcc00`, `#eab308`, `#fde68a` | Convite pendente, plano trial |
| Erro / cancelado | `#ef4444`, `#f43f5e` | Cancelado, recusado, excluir |
| Ligas | `#a855f7` | Tudo relacionado a ligas/convites de liga |
| Placar confirmado | `#2dd4bf` | Agenda — placar confirmado |

### Legenda da agenda (não alterar o significado das cores)
| Situação | Cor | Classe |
|---|---|---|
| Jogo confirmado (futuro) | `#25d366` verde | `.dot.game` |
| Realizado (placar informado/confirmado) | `#3b82f6` azul | `.dot.played` |
| Placar pendente (já começou, sem resultado) | `#a855f7` roxo | `.dot.score-pending` |
| Amistoso pendente (convite) | `#ffcc00` amarelo | `.dot.invite` |
| Cancelado / recusado / expirado | `#ef4444` vermelho | `.dot.cancelled` |

O "jogo já aconteceu" é contado a partir do **horário de início**, tanto para partidas oficiais quanto para as externas. Todo evento novo da agenda deve se encaixar nesta legenda.

Fundos de status usam a mesma cor com alpha baixo (ex.: `rgba(34,197,94,.14)`) e borda com alpha ~`.35`.
Ao precisar de uma cor nova, reutilize uma desta tabela; não invente tons novos.

## Tipografia

- Fonte do sistema, sem web font: `font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;` (páginas públicas: `"Segoe UI", Arial, sans-serif`). Não adicionar Google Fonts sem combinar antes.
- Pesos: 400 corpo, 600 menu/labels, 700–800 títulos e botões, 900 badges.
- Badges/etiquetas: `text-transform: uppercase`, ~`.74rem`, peso 900.
- Títulos grandes podem usar `clamp()` e `letter-spacing` negativo (ver `.hero h1`).

## Regras de layout e componentes

1. **Mobile first.** A maioria dos usuários acessa pelo celular. Em CSS novo, escreva o estilo base para telas pequenas e amplie com `@media (min-width: ...)`. No PrimeFlex, use `col-12` como padrão e acrescente `md:`/`lg:`. (O CSS legado usa `max-width`; ao mexer nele, não precisa reescrever tudo, mas não piore o mobile.)
2. Breakpoints de referência: 420px, 768px e **980px**. No `layout.xhtml`, até 980px a sidebar some e aparece a barra inferior fixa (Agenda, Buscar, Ranking, Chat, Dados, Suporte e Admin para o perfil ADMIN). O `styles.css` legado ainda usa 991px. Conteúdo novo precisa reservar espaço para a barra inferior e respeitar a safe area.
3. **Larguras de validação.** Toda tela nova ou alterada deve ser conferida, sem rolagem horizontal, sobreposição ou texto cortado, em:
   - **320px** — menor smartphone suportado (ponto de partida do layout);
   - **360px**, **390px** e **430px** — smartphones comuns;
   - **768px** — tablet;
   - **acima de 980px** — desktop (com sidebar).
   Lembre que o app Android abre o site em WebView: o que funciona em 320–430px no navegador precisa funcionar no app.
4. **Botões com cantos arredondados de 8px** — `border-radius: 8px`. Em `p:commandButton`/`p:button` use `styleClass="rounded-btn"`.
5. Outros raios: cards/painéis 12px, modais/cards grandes 16px, badges/pílulas 999px, avatares/escudos 50%.
6. Alvos de toque com no mínimo 44×44px. Botão primário público tem `min-height: 52px` (pequeno: 42px).
7. Sem rolagem horizontal da página. Tabelas largas vão dentro de `.am-table-scroll`.
8. Variantes de botão do PrimeFaces: primário (padrão), `ui-button-secondary`, `ui-button-success`, `ui-button-danger`, `ui-button-warning`, `ui-button-outlined`, `ui-button-sm` para compactos.
9. Hover: transição curta (`.15s`–`.2s`), clareando a cor ou `translateY(-2px)`. Respeite `prefers-reduced-motion`.
10. Acessibilidade: foco visível (`outline: 3px solid #65d7ff`), `alt` em imagens, contraste de texto claro sobre fundo escuro.
11. Textos da interface em português do Brasil.
12. Feedback com `p:messages`/`p:message`/`p:growl`. Toda ação Ajax deve atualizar a área de mensagens, e um erro de validação nunca pode apagar o que o usuário digitou.
13. Estilos de uma tela nova vão num CSS próprio com prefixo do módulo (ex.: `css/partidas-externas.css` com `registro-jogo-*`), carregado por `h:outputStylesheet` na página.

## Fluxo de trabalho

- Cada Jira tem uma branch com o mesmo nome (`ARENA-NN`), criada a partir da `main` atualizada. Integra primeiro em `dev`, depois em `main`.
- **Não fazer commit, push ou merge sem pedido explícito.** O usuário revisa antes.
- Antes de implementar, ler o Jira (escopo, decisões e critérios de aceite). Decisões comerciais em aberto, como qual plano libera um recurso, devem ser confirmadas com o usuário, nunca supostas.
- Validar com `mvnw clean test`, mais os testes de integração do módulo em banco descartável, e conferir a tela nas larguras da regra 3.
- **Atenção:** o `Dockerfile` usa `eclipse-temurin:17` e o projeto compila para Java 21. É uma divergência conhecida que deve ser alinhada antes de depender dele.
