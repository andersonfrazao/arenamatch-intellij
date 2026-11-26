# Arena Match — referência técnica da ARENA-70

Documento elaborado em 16/11/2026 a partir da estrutura, do código-fonte e das configurações locais da branch `ARENA-70`.

O levantamento contempla as alterações locais presentes durante a análise, incluindo o placar das partidas externas e a migration `V70.1__ARENA_70_placar_partida_externa.sql`. A aplicação das migrations em cada ambiente deve ser conferida em `flyway_schema_history`. Esta análise foi estática, com leitura dos arquivos do projeto.

## 1. Arquitetura e organização

### 1.1. Visão geral

O Arena Match é um monólito Spring Boot com interface JSF/Facelets, componentes PrimeFaces, APIs REST e persistência JPA em PostgreSQL. A interface e as APIs são empacotadas no mesmo JAR executável. Os clientes JSF fazem chamadas HTTP às APIs por meio do `RestClient` configurado em `AppConfig`.

```text
Navegador / aplicativo Android com WebView
                    |
             XHTML / PrimeFaces
                    |
              JSF Managed Bean
                    |
          Client HTTP com RestClient
                    |
              Controller REST
                    |
                  Service
                    |
          Repository / JPA / Hibernate
                    |
                PostgreSQL

Service -> SimpMessagingTemplate -> STOMP/SockJS -> navegador
```

O endereço das chamadas internas vem de `arenamatch.api.base-url`. O interceptor de `AppConfig` recupera `jwtToken` da sessão JSF e acrescenta o cabeçalho Bearer às requisições.

### 1.2. Tecnologias verificadas

| Componente | Configuração encontrada |
| --- | --- |
| Java | 21, declarado em `pom.xml` |
| Spring Boot | 3.5.9 |
| JoinFaces | `primefaces-spring-boot-starter` 5.3.4 |
| JSF e PrimeFaces | Dependências fornecidas pelo starter JoinFaces |
| Tema PrimeFaces | `arya` |
| PrimeFlex | 3.3.1, carregado por CDN no layout |
| Ícones | PrimeIcons e Font Awesome, com Font Awesome 6.4.0 no layout |
| Persistência | Spring Data JPA, Hibernate e driver PostgreSQL |
| Evolução do banco | Flyway com `flyway-database-postgresql` |
| Autenticação | Spring Security, JWT HS256 e BCrypt |
| Comunicação em tempo real | Spring WebSocket, STOMP e SockJS |
| E-mail | Spring Boot Mail |
| Utilitários de modelo | Lombok |
| Build | Maven Wrapper, distribuição configurada 3.9.14 |
| Empacotamento | Spring Boot Maven Plugin, JAR executável |
| Containers | Dockerfile e Docker Compose, com PostgreSQL 15 no Compose |
| Testes | JUnit Jupiter, Mockito, testes Spring MVC/JPA e teste JavaScript com Node |

As versões transitivas de JSF, PrimeFaces, Hibernate e Flyway podem ser consultadas na árvore de dependências do Maven.

### 1.3. Pacotes de `br.com.arenamatch`

| Pacote | Responsabilidade e exemplos |
| --- | --- |
| Raiz | `ArenamatchApplication`, com `@SpringBootApplication` e `@EnableScheduling` |
| `config` | Segurança, filtros, `RestClient`, rotas públicas, uploads, analytics e WebSocket |
| `controller` | Endpoints REST de autenticação, cadastro, busca, agenda, partidas, partidas externas, times, ligas, chat, notificações e administração |
| `service` | Regras de negócio, transações, conversões e coordenação dos casos de uso |
| `repository` | Interfaces Spring Data JPA, consultas JPQL e SQL nativo |
| `entity` | Entidades persistidas, relacionamentos e mapeamentos de colunas |
| `dto` | Contratos HTTP e dados preparados para as telas |
| `enums` | Planos, perfis, modalidades, categorias e estados dos fluxos |
| `jsf.beans` | Estado das telas e ações da interface, com beans como `CadastroBean`, `BuscaBean`, `MinhaAgendaBean` e `RegistroPartidaBean` |
| `jsf.client` | Clientes HTTP das APIs, como `CadastroClient`, `PartidaClient` e `PartidaExternaClient` |
| `integracao` | `ViaCepClient` e `GeoClient`, para endereço e geolocalização |
| `exception` | `GlobalExceptionHandler`, com tratamento centralizado das exceções REST |

Os beans de tela usam `@Named` e escopo de visão. `SessaoBean` mantém o estado da sessão autenticada. Há classes com injeção por construtor e classes legadas com `@Autowired` em campos.

Alguns serviços funcionam como fachadas. `PartidaService` delega desafios, cancelamento e placares para `DesafioPartidaService`, `CancelamentoPartidaService` e `PlacarService`. O cadastro também possui serviços próprios de validação, usuário, time, agenda, endereço e mapeamento.

### 1.4. Diretórios e entradas principais

```text
pom.xml
mvnw / mvnw.cmd
.mvn/wrapper/maven-wrapper.properties
Dockerfile
docker-compose.yml
src/main/java/br/com/arenamatch/
src/main/resources/
  application.properties
  application-dev.properties
  application-prod.properties
  db/migration/
  META-INF/resources/
    WEB-INF/template/
    css/
    js/
    images/
    *.xhtml
src/test/java/br/com/arenamatch/
src/test/js/analytics.test.cjs
docs/
prototipos/gestao-campeonatos/
target/
```

| Experiência | Arquivos principais |
| --- | --- |
| Apresentação pública | `index.xhtml`, `como-funciona.xhtml`, `encontrar-times-futebol-amador.xhtml` e `privacidade.xhtml` |
| Acesso e cadastro | `login.xhtml`, `cadastro.xhtml`, `ativar-conta.xhtml` e `recuperar-senha.xhtml` |
| Agenda e registro manual | `minha-agenda.xhtml` e `registrar-jogo.xhtml` |
| Busca de adversários | `busca-time.xhtml` |
| Desempenho | `ranking.xhtml` e `adversario-detalhes.xhtml` |
| Comunicação | `chat.xhtml` e painel de notificações do layout |
| Ligas | `minhas-ligas.xhtml`, `explorar-ligas.xhtml` e `gerenciar-liga.xhtml` |
| Administração e suporte | `admin.xhtml`, `dashboard.xhtml` e `fale-conosco.xhtml` |

O template privado compartilhado é `META-INF/resources/WEB-INF/template/layout.xhtml`. Cabeçalho, rodapé, FAQ e componentes de analytics públicos também ficam em `WEB-INF/template`. `docs/` contém documentação funcional, SEO, analytics, migrations e materiais da Play Store. `prototipos/gestao-campeonatos/` contém protótipos HTML/CSS/JavaScript.

### 1.5. Autenticação, acesso e erros

- `FiltroAutenticacao` controla a navegação JSF usando a sessão, com redirecionamento para login e tratamento de resposta parcial AJAX.
- `SecurityConfig` e `JwtAuthenticationFilter` controlam as APIs. `/api/**` exige autenticação, respeitando as exceções públicas declaradas. `/api/admin/**` exige `ROLE_ADMIN`.
- Login, recuperação, ativação e segundo fator administrativo possuem rotas públicas próprias. O cadastro inicial libera explicitamente os endpoints de validação, disponibilidade, escudo e consulta de endereço necessários ao assistente.
- `JwtService` gera tokens HS256 com assunto, ID, perfil e expiração. A duração tem fallback de 43.200 segundos e pode ser configurada por `arenamatch.jwt.expiration-seconds`.
- A autenticação das APIs é comum. As verificações de propriedade são feitas nos serviços que atendem cada caso de uso. `PartidaExternaService` identifica o proprietário pelo usuário autenticado, enquanto vários contratos existentes recebem IDs nos parâmetros ou DTOs.
- `GlobalExceptionHandler` preserva o status de `ResponseStatusException`, trata `RuntimeException` como erro 400 e registra `[REGRA DE NEGOCIO BARRADA]`. Falhas inesperadas produzem erro 500 com mensagem genérica. Parte das respostas de erro usa texto simples.

## 2. Padrões visuais

### 2.1. Dark mode e glassmorphism

A identidade visual combina fundos azul-marinho, painéis escuros, texto claro e ações em ciano. O tema PrimeFaces `arya` é complementado por CSS próprio e por regras no template.

| Uso | Referências encontradas |
| --- | --- |
| Fundo público | `#0b1220` |
| Painel público | `#111d30` |
| Sidebar e navegação | `rgba(15, 23, 42, 0.95)` |
| Superfície de vidro | `rgba(30, 41, 59, 0.7)` |
| Destaques e ações privadas | `#0ea5e9` e `#38bdf8` |
| Destaque público | `#19b5ee` |
| Texto principal | `#e2e8f0` e `#f3f6fc` |
| Texto secundário | `#94a3b8` e `#a8b6cc` |
| Bordas | Branco com baixa opacidade, `#25364c` e `#334155` |
| Feedback | Verde para sucesso, amarelo para alertas e limitações, vermelho para erro ou cancelamento |

O efeito de vidro usa transparência, bordas claras discretas, sombras e `backdrop-filter`, normalmente entre 10 e 12 px. Cartões, campos e botões possuem cantos arredondados. A tipografia usa principalmente Segoe UI e fontes de sistema.

### 2.2. Layout e responsividade

- PrimeFlex fornece utilitários de grid, flexbox, largura, espaçamento e alinhamento. Formulários usam reorganização de colunas conforme o espaço disponível.
- O layout privado atual usa classes `am-*`. Estilos específicos de páginas usam prefixos próprios, como `registro-jogo-*`.
- Desde o épico mobile first (ARENA-87, em produção), todas as telas seguem `docs/frontend-mobile-first.md`: CSS base para o celular e ampliação por `min-width` em 576, 768, 992 e 1200 px.
- A partir de 992 px a navegação principal fica na sidebar; abaixo disso aparece a barra inferior fixa (Agenda, Buscar, Ranking, Chat, Dados e Suporte, mais Admin para o perfil administrativo). A área de conteúdo já reserva o espaço da barra.
- As páginas públicas usam cabeçalho próprio, com menu recolhido no celular e "Entrar" e "Criar conta" sempre visíveis (ARENA-100).
- Componentes `p:messages`, `p:growl` e `p:message` mostram validações e resultados das ações. As atualizações AJAX incluem esses componentes quando há mensagens a apresentar.

### 2.3. Arquivos de estilo e comportamento

| Arquivo | Uso |
| --- | --- |
| `css/styles.css` | Estilos globais, campos, superfícies de vidro e elementos compartilhados |
| `WEB-INF/template/layout.xhtml` | Estrutura privada, estilos de navegação e integração de notificações |
| `css/institucional.css` | Identidade e layout das páginas públicas |
| `css/login-publico.css` | Apresentação e navegação pública junto ao login |
| `css/guia-publico.css` | Guia visual de utilização |
| `css/amistosos.css` | Conteúdo público sobre amistosos |
| `css/partidas-externas.css` e `js/registro-jogo.js` | Formulário de registro manual (o cartão na agenda usa `css/agenda.css`, e o atalho, `css/busca.css`) |
| `js/menu-publico.js` e `js/login-publico.js` | Interações dos menus públicos |
| `js/flyer.js` | Recursos de geração de imagem disponíveis nas telas que o utilizam |
| `js/analytics.js` e `css/analytics.css` | Comportamento e apresentação relacionados a analytics |

Para evoluir telas, reutilizar o template, os componentes PrimeFaces e as classes existentes. Manter rótulos legíveis, foco visível, contraste e espaço para toque. O CSS público já inclui foco destacado e tratamento de movimento reduzido.

## 3. Regras de negócio

### 3.1. Cadastro, responsável e modalidades

O cadastro segue as etapas de responsável, time e agenda. O modelo associa o time ao usuário responsável por `@OneToOne`. CPF e celular são normalizados, a senha é armazenada com BCrypt e o aceite dos termos possui data registrada.

A ativação de e-mail é configurável. Quando habilitada, o usuário nasce com `PENDENTE_ATIVACAO` e recebe código com validade de 15 minutos. O cadastro inicia o plano e o pagamento em `TRIAL`, com expiração calculada pelos parâmetros do sistema.

`Modalidade` contém `FUTEBOL_CAMPO`, `FUTSAL`, `FUT_7` e `FUTEBOL_SOCIETY`. Um time pode acumular modalidades. Na edição, `TimeCadastroService` exige que todas as modalidades já persistidas continuem presentes e acrescenta as novas seleções.

Cada disponibilidade associa modalidade, categoria, dia da semana, início e fim. A interface permite escolher modalidade e categoria ao adicionar horários. As modalidades existentes são consolidadas no cadastro do time. Os dados anteriores à ARENA-67 são migrados para Futebol de Campo.

As categorias atuais são Esporte Livre, Mesclado, Veterano 35+, Veterano 40+, Master 50+ e Sub-20. A conversão de `CategoriaDTO` ainda aceita ordinal, além da descrição. A ordem do enum participa desse contrato.

### 3.2. Planos e parâmetros

O plano pertence a `Usuario`, nos campos `planoAssinatura`, `statusPagamento` e datas associadas. `AssinaturaService` centraliza o acesso completo.

| Plano | Regra implementada |
| --- | --- |
| Básico | Busca sujeita ao limite de raio e criação de desafios sujeita ao intervalo configurado |
| Trial | Acesso completo enquanto plano e pagamento estão em `TRIAL` e a expiração é futura |
| PRO | Acesso completo quando `statusPagamento` é `PAGO` |
| PREMIUM | Valor reconhecido no enum, com `incluiPro()` verdadeiro e acesso PRO condicionado a pagamento `PAGO` |

Trials expirados passam para `BASICO` e pagamento `EXPIRADO`, tanto na atualização individual quanto na tarefa periódica. Usuários legados com plano vazio recebem plano sincronizado pelo estado do pagamento.

`StatusPagamento` contém `TRIAL`, `PENDENTE`, `PAGO`, `ATRASADO`, `CANCELADO` e `EXPIRADO`. O modelo possui campos de gateway e assinatura externa. Nesta branch, o controle de acesso está implementado em `AssinaturaService` e nos serviços consumidores. A integração recorrente de pagamento pertence a uma evolução adicional do domínio.

Os valores abaixo são os padrões do código e das migrations de parâmetros. A tabela `parametro_sistema` pode fornecer valores diferentes em cada ambiente.

| Chave | Padrão |
| --- | --- |
| `MIN_DIAS_ANTECEDENCIA_AGENDAMENTO` | 3 dias |
| `MIN_DIAS_ANTECEDENCIA_CANCELAMENTO` | 3 dias |
| `DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO` | 15 dias |
| `DIAS_TRIAL` | 90 dias |
| `DIAS_CONFIRMACAO_AUTOMATICA_PLACAR` | 3 dias |
| `RAIO_MAXIMO_BUSCA_PLANO_BASICO_KM` | 10 km |

O intervalo do Básico é verificado na criação de desafio a partir das partidas futuras ativas encontradas pelo repositório. O código calcula a próxima data permitida a partir da partida retornada e compara com o dia atual. O valor zero desativa essa restrição.

### 3.3. Busca e desafios

1. A busca exige modalidade cadastrada no próprio time e data válida, respeitando a antecedência mínima.
2. O time precisa ter disponibilidade semanal na modalidade e no dia solicitado.
3. A consulta combina localização, raio, nome, categoria, modalidade, agenda compatível e mando de campo oposto. O raio do Básico é limitado no backend.
4. A apresentação inclui modalidade, categoria, horário do mandante, taxa, distância e situação do convite. Histórico de confrontos é enriquecido para acesso completo.
5. O desafio valida times ativos, modalidade dos dois participantes, disponibilidade e regras de plano. O horário é definido pela agenda do mandante.
6. A partida nasce em `PENDENTE`, com referência ao desafiante, mensagem inicial e aviso WebSocket ao destinatário.
7. O aceite verifica ocupação e pendências, obtém locks dos times em ordem de ID e passa a partida para `AGENDADO`.
8. Convites pendentes podem ser excluídos. A tarefa de expiração considera vencidos os convites cuja data é o dia atual ou anterior.

Os estados de `StatusPartida` são `PENDENTE`, `AGENDADO`, `SOLICITACAO_CANCELAMENTO`, `CANCELADO`, `FINALIZADO` e `EXPIRADO`.

As regras de ocupação têm granularidades diferentes nos fluxos atuais. Partidas oficiais agendadas bloqueiam o dia nas consultas existentes. Os registros externos participam das verificações com a modalidade selecionada. Essa distinção deve ser preservada ao alterar a busca ou o aceite de desafios.

### 3.4. Agenda e cancelamento

`agenda` armazena disponibilidades semanais. `AgendaService` monta a agenda por data usando partidas oficiais e registros externos, ordena os eventos e prepara a lista de próximos jogos.

O calendário apresenta indicadores de jogo confirmado, convite pendente, cancelamento, placar pendente e jogo realizado. A exibição considera o horário de início e o estado do placar. Os DTOs distinguem `idPartida` de `idPartidaExterna` e identificam os registros manuais.

O cancelamento de jogo agendado exige participação do solicitante, motivo de até 350 caracteres e antecedência suficiente. A solicitação cria `SOLICITACAO_CANCELAMENTO` e uma mensagem no chat. O outro participante pode aceitar, levando a `CANCELADO`, ou recusar, retornando a `AGENDADO`.

### 3.5. Placares, ranking e Meu Scout

O placar oficial possui os estados `PENDENTE`, `AGUARDANDO_CONFIRMACAO`, `CONFIRMADO` e `EM_DISPUTA`. Informar o resultado registra o time informante e a data, além de notificar o adversário. A confirmação computa estatísticas e a contestação coloca o resultado em disputa.

`PlacarPendenteService` usa lock pessimista na confirmação e retorna imediatamente se o resultado já estiver confirmado. A pontuação acumulada concede 3 pontos por vitória e 1 por empate. Também atualiza jogos, vitórias, empates, derrotas e gols em `Time`.

O ranking por modalidade é calculado por `PartidaRepository.buscarRankingPorModalidade`, agregando partidas com `status_placar = 'CONFIRMADO'`. A participação na agregação exige ao menos um jogo confirmado naquela modalidade. A ordenação atual usa vitórias, saldo de gols, gols pró e ID como desempate final.

`TimeService` usa essa consulta para ranking e estatísticas por modalidade. Os campos acumulados de `Time` continuam presentes para compatibilidade. Ranking, Meu Scout e detalhes de desempenho exigem acesso completo, com tratamento específico para o perfil ADMIN.

Pendências de placar podem bloquear busca, novos desafios, aceite e outras operações que chamam `validarSemPlacarPendente`. Um resultado aguardando confirmação é confirmado automaticamente após o prazo configurado. A rotina horária e a consulta de pendências executam essa verificação.

### 3.6. Registro manual de jogos na ARENA-70

O fluxo permite registrar jogos combinados fora da plataforma, futuros ou retroativos, com adversário externo vinculado ao proprietário. Os registros ficam nas tabelas `adversario_externo` e `partida_externa` e integram a agenda do responsável.

- O serviço resolve o time pelo JWT, exige usuário ativo e restringe leitura à agenda desse time. O registro manual está disponível aos planos atendidos pela autenticação, com validações próprias.
- Obrigatórios: adversário, modalidade, categoria e data. A data precisa cair num dia da semana da agenda do cadastro (tabela `agenda`) para a modalidade e a categoria escolhidas, como nos desafios. O calendário bloqueia os outros dias (`disabledDays`) e o serviço repete a validação.
- O mando é derivado do cadastro do time no backend. **Mandante:** horário (da agenda daquele dia), campo, endereço e taxa vêm do cadastro e aparecem só para leitura ("Taxa do visitante"); o que chegar do formulário é ignorado. **Visitante:** informa os dados do mandante, todos opcionais. Sem horário, vale o da agenda do time naquele dia; só com o início, o jogo dura 2 horas. Campo e taxa em branco ficam nulos (V70.2) e aparecem na agenda como "A combinar" e "Não informada".
- Telas (refeitas no mobile first após o merge da `main`): `registrar-jogo.xhtml` (`css/partidas-externas.css`, `js/registro-jogo.js`), botão "Registrar jogo" na Minha Agenda (também no dia vazio), atalho na busca abaixo dos filtros (com destaque quando não há adversários) e cartão "Registrado por você" na agenda, com as cores da legenda e flyer para jogo futuro.
- O adversário pode ser digitado ou reaproveitado. Nome normalizado e proprietário identificam o cadastro externo. A partida também conserva o nome informado.
- A modalidade deve pertencer ao time e permanece imutável. Um fim digitado deve ser posterior ao início na mesma data; um horário da agenda que passa da meia-noite (ex.: 22:00 às 00:00) termina no dia seguinte.
- A taxa usa `BigDecimal`, opcional, com até duas casas decimais e valor mínimo zero.
- A chave UUID por time torna o reenvio idempotente. Repetir a chave com os mesmos dados retorna o registro existente. Dados diferentes produzem conflito 409.
- O lock do time serializa cadastros concorrentes. O serviço verifica sobreposição com jogos externos da mesma modalidade e jogo oficial confirmado da mesma modalidade no dia.
- O placar opcional aceita os dois valores juntos, de 0 a 99. Empate sem gols exige preenchimento explícito de ambos os zeros. O início da partida deve ter ocorrido para aceitar o placar.
- A situação é calculada como `AGENDADO`, `AGUARDANDO_PLACAR` ou `REALIZADO`. O calendário aproveita esses estados para suas legendas.

As estatísticas oficiais e o ranking são derivados da tabela `partida`. O placar manual fica armazenado em `partida_externa`, com apresentação própria na agenda. Os endpoints atuais desse recurso oferecem criação, consulta de opções e leitura individual.

### 3.7. Ligas, chat, notificações e tarefas

As ligas agrupam times, possuem um time administrador, convites, solicitações de entrada e chat próprio. A associação é persistida em `time_liga`. A criação inclui o administrador como membro e a remoção de membros preserva esse responsável.

O chat de partida persiste mensagens com remetente e estado de leitura. `ChatBloqueioPolicy` encerra o envio para partida cancelada, placar confirmado ou convite expirado, incluindo convite pendente com data atual ou anterior.

O WebSocket usa endpoint `/ws-arenamatch`, fallback SockJS e broker simples em memória com prefixo `/topic`. Os serviços persistem as alterações e usam `SimpMessagingTemplate` para publicar eventos. Os envios e as consultas de chat passam pelas APIs REST.

| Canal | Uso |
| --- | --- |
| `/topic/notificacoes/{idTime}` | Avisos como `CHEGOU_CONVITE` e `CHEGOU_CHAT`, seguidos de atualização AJAX dos painéis |
| `/topic/chat/{idPartida}` | Mensagens da conversa de partida |
| `/topic/chat/liga/{idLiga}` | Mensagens da conversa da liga |

O template inclui reconexão WebSocket após falha. O broker simples mantém as inscrições dentro da instância da aplicação. Uma implantação com múltiplas instâncias exige considerar essa característica na distribuição dos eventos.

| Tarefa agendada | Frequência configurada |
| --- | --- |
| Conversão de Trial expirado | Atraso de 60 segundos entre execuções |
| Expiração de convites | Atraso de 60 segundos entre execuções |
| Confirmação automática de placar | Atraso de 3.600.000 ms entre execuções |

## 4. Banco de dados e Flyway

### 4.1. Estrutura principal

| Tabela | Dados e relacionamentos principais |
| --- | --- |
| `usuario` | Identidade, CPF, contato, senha, perfil, ativação, recuperação, aceite dos termos e campos de plano/pagamento |
| `time` | Responsável, nome, endereço, coordenadas, mando, campo, taxa, escudo e estatísticas acumuladas |
| `time_modalidade` | Modalidades do time, com chave composta `(id_time, modalidade)` |
| `agenda` | Disponibilidades semanais do time por modalidade e categoria |
| `partida` | Mandante, visitante, desafiante, data, modalidade, status, cancelamento e placar oficial |
| `liga` | Nome, descrição, criação e time administrador |
| `time_liga` | Associação entre times e ligas, com informação de entrada |
| `convite_liga` | Convite ou solicitação de entrada, mensagem, data e estado |
| `mensagem_chat` | Mensagens vinculadas à partida e ao time remetente |
| `mensagem_chat_liga` | Mensagens vinculadas à liga e ao time remetente |
| `notificacao` | Destinatário, tipo, referência, conteúdo, leitura e criação |
| `parametro_sistema` | Chave, valor e descrição das regras configuráveis |
| `adversario_externo` | Adversário privado do time, nome e nome normalizado |
| `partida_externa` | Registro manual, proprietário, adversário, modalidade, categoria, mando, período, local, taxa, placar, autor e auditoria |
| `flyway_schema_history` | Histórico de versões, scripts, checksums e resultado das migrations |

Os IDs usam estratégias de geração declaradas por entidade, incluindo sequences explícitas e `IDENTITY`. Enums são normalmente persistidos como texto. Disponibilidades legadas usam horários em campos de texto, enquanto os jogos usam `LocalDateTime` e colunas `TIMESTAMP`.

O cadastro semanal referencia a modalidade do time por chave estrangeira composta. Partidas oficiais referenciam a modalidade dos dois participantes. Partidas externas referenciam a modalidade do proprietário e usam chave composta para assegurar que o adversário pertença ao mesmo time.

O banco protege a modalidade das partidas por trigger `modalidade_partida_imutavel`. A tabela externa usa o mesmo mecanismo. Índices apoiam consulta de agenda, modalidade, placar e notificações. A idempotência manual também possui restrição única `(id_time, chave_solicitacao)`.

### 4.2. Histórico e migrations recentes

Os scripts ficam em `src/main/resources/db/migration`. A árvore analisada contém 38 migrations. O histórico começa em `V1`, passa pelas versões até `V34` e inclui `V39`, `V67.0`, `V70.0` e `V70.1`.

| Versão ou arquivo | Evolução |
| --- | --- |
| V1 a V8 | Usuário, time, contato, endereço, agenda, localização e partida |
| V9 a V18 | Ligas, convites, associações, mensagens e leitura do chat |
| V19 a V23 | Recuperação, ranking/placar, notificações, termos e plano/pagamento |
| V24 a V28 | Parâmetros, ativação de e-mail, Trial, confirmação automática e raio do Básico |
| `V29__corrigir_horario_partida_pelo_mandante.sql` | Correção de horários conforme o mandante |
| `V30__adicionar_parametro_cancelamento_partida.sql` | Antecedência mínima de cancelamento |
| `V31__adicionar_escudo_time.sql` | Escudo do time |
| `V32__adicionar_segundo_fator_admin.sql` | Campos do segundo fator administrativo |
| `V33__remover_status_assinatura_usuario.sql` | Remoção do campo antigo de status da assinatura |
| `V34__atualizar_email_admin_arena.sql` | Atualização cadastral administrativa |
| `V39__adcionar_nome_campo_time.sql` | Nome do campo do time, preservando a grafia do arquivo existente |
| `V67.0__ARENA_67_adicionar_modalidades_agenda_partida.sql` | `time_modalidade`, backfill para Campo, FKs compostas, índices e trigger de modalidade |
| `V70.0__ARENA_70_registrar_partidas_externas.sql` | Adversários e partidas externas, sequences, auditoria, idempotência e índices |
| `V70.1__ARENA_70_placar_partida_externa.sql` | Placar opcional, preenchimento conjunto e limites de 0 a 99, presente nas alterações locais |
| `V70.2__ARENA_70_campo_taxa_opcionais_partida_externa.sql` | `nome_campo` e `valor_taxa` aceitam nulo (visitante sem dados do mandante; mandante sem campo ou taxa no cadastro) |

### 4.3. Convenção das próximas migrations

A convenção registrada em `docs/migracoes-flyway.md` usa o número do Jira como versão numérica e uma sequência iniciada em zero dentro da história.

```text
V67.0__ARENA_67_adicionar_modalidades_agenda_partida.sql
V67.1__ARENA_67_descricao_da_proxima_alteracao.sql
V70.0__ARENA_70_registrar_partidas_externas.sql
V70.1__ARENA_70_placar_partida_externa.sql
```

- Usar `V<numero-do-jira>.<sequencia>__ARENA_<numero-do-jira>_<descricao>.sql`.
- Preservar versões e conteúdo dos scripts já aplicados. Acrescentar uma migration para novas alterações.
- Manter cada versão única entre branches e considerar dependências entre scripts.
- `spring.flyway.out-of-order=true` está configurado nos arquivos de aplicação e permite executar versões pendentes inferiores à maior versão aplicada.
- Os arquivos base e dev também configuram tolerância a migrations futuras/ausentes. A propriedade legada `spring.flyway.ignore-missing-migrations` aparece nesses arquivos junto de `spring.flyway.ignore-migration-patterns`.
- Rodar `clean` ao reconstruir após renomear scripts locais ainda pendentes, para remover cópias antigas de `target/classes`.

O Flyway executa as migrations no startup. O estado aplicado e os parâmetros efetivos dependem do banco selecionado no ambiente.

## 5. Convenções de código e operação

### 5.1. Convenções observadas

- Usar nomes de domínio em português, como `Partida`, `Time`, `buscarEventos` e `registrar`, preservando os sufixos de camada `DTO`, `Service`, `Repository`, `Controller`, `Client` e `Bean`.
- Manter a sequência XHTML → Bean → Client HTTP → Controller → Service → Repository nos casos de uso da interface.
- Transportar DTOs entre interface e API. Concentrar validações de negócio e controle transacional nos serviços.
- Reutilizar serviços de plano, parâmetros, horários, notificações e placares ao alterar seus fluxos.
- Preservar nomes persistidos dos enums, IDs e contratos JSON ao evoluir dados existentes. A conversão ordinal de categoria merece atenção em alterações na ordem do enum.
- Usar `@Transactional` em operações coordenadas de escrita e `readOnly` nas consultas que seguem esse padrão. Respeitar os locks existentes nos fluxos concorrentes.
- Usar `BigDecimal` no domínio monetário de partidas externas. O cadastro de time ainda possui taxa em `Double`, com conversão nas opções de registro manual.
- Manter validações JSF e mensagens próximas aos campos. As chamadas AJAX devem processar os campos da ação e atualizar as áreas de feedback relevantes.
- Reutilizar os estilos e templates de cada experiência. Os textos de interface seguem o português e apresentam rótulos, mensagens e descrições dos enums.

### 5.2. Configuração dos ambientes

Os arquivos `application.properties`, `application-dev.properties` e `application-prod.properties` contêm as configurações base e por perfil. Spring Boot também aceita sobrescrita por variáveis de ambiente e argumentos de execução.

| Propriedade | Finalidade |
| --- | --- |
| `spring.datasource.*` | Conexão PostgreSQL |
| `spring.flyway.*` | Execução e comportamento das migrations |
| `arenamatch.api.base-url` | URL das APIs chamadas pelos clientes JSF |
| `arenamatch.jwt.secret` e `arenamatch.jwt.expiration-seconds` | Assinatura e expiração do JWT |
| `spring.mail.*` | Servidor e autenticação de e-mail |
| `google.maps.apikey` | Integrações de geolocalização |
| `arenamatch.validation.cpf-enabled` | Validação de CPF |
| `arenamatch.validation.email-activation-enabled` | Ativação por e-mail |
| `arenamatch.uploads.escudos-dir` | Diretório físico dos escudos |
| `arenamatch.pagina-publica-inicial-habilitada` | Destino da raiz `/` |
| `arenamatch.analytics.enabled` e `arenamatch.analytics.measurement-id` | Habilitação e identificador GA4 |

Na árvore analisada, os três arquivos de aplicação definem `arenamatch.pagina-publica-inicial-habilitada=true`. Com esse valor, `/` redireciona para `/index.xhtml`. Com `false`, redireciona para `/login.xhtml`. O fallback da classe `WebConfig` é `false` quando a propriedade está ausente.

O aplicativo Android pode abrir `/login.xhtml` diretamente. Uma WebView que abre `/` acompanha a configuração acima. O código Flutter pertence ao projeto Android separado.

Os escudos são servidos por `/uploads/escudos/**`, usando o diretório configurado, cujo fallback é `/app/uploads/escudos`. Os limites multipart declarados são 2 MB por arquivo e 3 MB por requisição. `AnalyticsConfig` habilita medição apenas para sua lista de páginas públicas e valida o formato do ID GA4.

### 5.3. Build e execução local

Executar a partir da raiz do repositório, com JDK 21 selecionado e PostgreSQL acessível. No primeiro uso, o Maven Wrapper pode baixar a distribuição e as dependências.

```powershell
# Conferir as versões utilizadas
java -version
.\mvnw.cmd -version

# Compilar e executar os testes habilitados
.\mvnw.cmd clean test

# Gerar o JAR executável
.\mvnw.cmd clean package

# Executar pelo Maven com perfil dev
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Executar o JAR já construído
java -jar target/arenamatch-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Consultar as versões efetivamente resolvidas
.\mvnw.cmd dependency:tree
```

Em Linux ou macOS, usar `./mvnw` nos comandos Maven correspondentes. O projeto gera `target/arenamatch-0.0.1-SNAPSHOT.jar`. A execução Spring Boot usa normalmente a porta 8080, sujeita à configuração do ambiente.

### 5.4. Testes existentes

Os testes Java cobrem cadastro, CEP, segurança pública, planos, modalidades, busca, desafios, placares, chat e registro manual. Os testes de repositório e migrations usam PostgreSQL real e são habilitados por variáveis de ambiente.

| Grupo | Variáveis |
| --- | --- |
| Modalidades e migration ARENA-67 | `ARENA67_TEST_DB_URL`, `ARENA67_TEST_DB_USER`, `ARENA67_TEST_DB_PASSWORD` |
| Partidas externas ARENA-70 | `ARENA70_TEST_DB_URL`, `ARENA70_TEST_DB_USER`, `ARENA70_TEST_DB_PASSWORD` |

Usar banco PostgreSQL de testes descartável. As suítes criam dados, executam migrations e incluem testes de concorrência. O usuário de conexão tem fallback `postgres` nesses testes. Sem a variável de URL correspondente, os testes condicionais daquele grupo ficam desabilitados.

```powershell
# Executar uma classe específica
.\mvnw.cmd "-Dtest=PartidaExternaServiceTest" test

# Executar os testes JavaScript de analytics
node --test src/test/js/analytics.test.cjs
```

Os relatórios Java são gerados em `target/surefire-reports`. Os comandos desta seção documentam o uso das ferramentas. A geração deste documento envolveu análise estática do projeto.

### 5.5. Docker e deploy manual

O `docker-compose.yml` define os serviços `db` e `app`. O banco usa `postgres:15` e volume nomeado `postgres_data`. A aplicação usa o build do Dockerfile, perfil `prod`, variáveis de conexão e mapeamento `80:8080`. Ambos os serviços usam `restart: unless-stopped`.

O Dockerfile copia um JAR previamente gerado de `target/` para `/app/app.jar` e inicia `java -jar app.jar`. O build Maven ocorre antes do build da imagem.

**Divergência encontrada no Dockerfile.** A imagem base atual é `eclipse-temurin:17-jre-alpine`, enquanto o projeto compila para Java 21. O runtime da imagem precisa ser alinhado a Java 21 para executar o artefato produzido pela configuração atual. Esse ajuste é uma pendência do arquivo de deploy.

Após alinhar o runtime e configurar as conexões do ambiente, a sequência de deploy manual com Compose é a seguinte.

```powershell
.\mvnw.cmd clean package
docker compose build app
docker compose up -d
docker compose ps
docker compose logs --tail 100 app
```

O volume declarado pelo Compose persiste o PostgreSQL. O diretório de escudos é configurado na aplicação e seu armazenamento persistente precisa acompanhar a infraestrutura de deploy. O arquivo Compose analisado declara o volume do banco.

No startup, conferir a conexão PostgreSQL, as migrations e a inicialização da aplicação nos logs. A verificação funcional pode começar pelo login e pela página pública, seguindo pelo cadastro, agenda e busca. Os detalhes do servidor em produção dependem da configuração efetivamente utilizada no deploy manual.

## 6. Referências internas para manutenção

- Arquitetura e dependências em `pom.xml` e `src/main/java/br/com/arenamatch/`.
- Layout compartilhado em `src/main/resources/META-INF/resources/WEB-INF/template/layout.xhtml`.
- Evolução do schema em `src/main/resources/db/migration/` e convenção em `docs/migracoes-flyway.md`.
- Páginas públicas em `docs/ARENA-62-paginas-publicas.md`.
- Analytics em `docs/ARENA-65-analytics.md`.
- Conteúdo público em `docs/ARENA-66-conteudo-publico.md` e `docs/SEO-primeiros-passos.md`.
- Empacotamento e containers em `Dockerfile`, `docker-compose.yml` e `.mvn/wrapper/maven-wrapper.properties`.

Esta referência deve acompanhar a evolução do código, dos parâmetros e das migrations da branch utilizada.
