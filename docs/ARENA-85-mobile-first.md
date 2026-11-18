# ARENA-85 — Administração e suporte

## Base e escopo

Branch ARENA-85 criada a partir da main atualizada (`8b8720a`), incorporando as entregas locais até ARENA-84 (`8bcf8a8`). Esta é a décima história da sequência vigente de 11 histórias do épico ARENA-74.

Conforme orientação do usuário, `dashboard.xhtml` não foi alterado. Ligas permanecem fora do escopo. A entrega contempla somente administração e suporte.

## Implementação

- `css/administracao-suporte.css` concentra a apresentação das duas páginas, com classes próprias. Pesquisa, edição e parâmetros começam com uma coluna. Os campos de edição passam a duas colunas a partir de 40 rem; a classificação de usuários passa a tabela a partir de 64 rem.
- A administração utiliza um único componente PrimeFaces para cartões no celular e tabela no desktop. Cada registro mantém nome completo, e-mail, perfil, status, plano, validade e ação Editar. A tabela possui rolagem local quando necessário, orientação visível e associação acessível.
- A pesquisa mantém o limite de 20 registros do backend e apresenta cinco registros por página. A navegação tem controles identificados e informa a página atual.
- A seleção do usuário leva o foco ao painel de edição. Mensagens de validação e sucesso ficam dentro do formulário, com foco após a resposta. Os controles têm rótulos associados e nomes de ações contextualizados.
- Pesquisa, seleção, salvamento de usuário, recarga dos parâmetros e salvamento de cada parâmetro processam e atualizam somente os componentes correspondentes. Campos de edição de outro contexto não bloqueiam essas ações.
- Suporte ganha composição própria, campos dimensionados para o celular, rótulos, contador associado à mensagem e respostas dentro do formulário.
- A troca de assunto atualiza somente as mensagens e a orientação de exclusão, preservando o texto ainda não enviado. Cancelar a solicitação também preserva esse texto.
- O diálogo de exclusão explica o fluxo existente: confirmar a orientação permite preencher e enviar uma solicitação ao suporte. Essa confirmação não executa uma exclusão automática. O diálogo mantém ações empilhadas no celular, conteúdo rolável e altura limitada ao viewport. O foco inicial fica em Cancelar, e o fechamento retorna ao formulário.
- Indicadores de carregamento, mensagens de falha de atualização e recuperação de foco são tratados em `js/administracao-suporte.js`, reutilizando o apoio compartilhado de acesso.

As regras de autorização, edição e suporte permanecem no backend existente. Não houve alteração de banco nem novas migrations.

## Correção de foco no suporte

Depois de tentar enviar um formulário incompleto, o campo Mensagem mantém o estado visual de validação até uma nova submissão. Uma atualização apenas do assunto não deve usar esse estado anterior para decidir o foco.

O encerramento dessa interação parcial foi separado da validação de envio. Assim, o seletor continua focado ao trocar o assunto e a mensagem digitada permanece intacta. Dois testes JavaScript cobrem essa regressão e a preservação do foco no aviso quando há falha de transporte.

## Verificação em 18/11/2026

Todas as alterações de dados e os envios utilizaram contas fictícias, PostgreSQL 15 e SMTP de teste locais. Não houve envio de mensagem ao serviço de e-mail de produção, remoção de conta ou alteração de acesso de pessoas reais.

| Verificação | Resultado |
| --- | --- |
| Build | Maven Wrapper `verify` aprovado; empacotamento final aprovado após a correção de foco |
| Java | 123 testes aprovados, sem falhas, erros ou ignorados |
| JavaScript | 71 testes aprovados, incluindo duas regressões do suporte; sintaxe do novo script conferida |
| Matriz responsiva | 36 combinações de usuários/edição, parâmetros, suporte e diálogo; sem largura de página superior ao viewport |
| Dimensões CSS | 320 × 780, 360 × 780, 390 × 844, 768 × 1024, 992 × 900, 1024 × 900, 1440 × 900, 844 × 390 e 320 × 430 |
| Áreas de toque | Botões, seletores e controles de paginação visíveis medidos em pelo menos 44 × 44 px nas combinações verificadas |
| Pesquisa | Validação de menos de três caracteres, resultados com nome extenso e estado sem usuários encontrados |
| Paginação | Seis usuários fictícios em duas páginas. Nova pesquisa após a segunda página apresentou corretamente um único resultado |
| Edição | Seleção de usuário, erro de data inválida e salvamento válido de um registro fictício sem mudar seu perfil ou plano |
| Parâmetros | Valor inválido apresenta mensagem; salvar o valor original apresenta sucesso. Um rascunho de outro parâmetro, com valor 16, permaneceu no campo sem ser salvo; Recarregar recuperou o valor persistido 15 |
| Independência | Data inválida na edição de usuário não bloqueou salvamento de parâmetro, recarga ou nova pesquisa |
| Suporte | Campos obrigatórios, preservação de texto após mudar assunto e cancelar, navegação do seletor pelo teclado e abertura pelo controle de lista |
| Envio | Mensagem de teste aceita pelo SMTP local; confirmação visível e campos limpos depois do sucesso |
| Diálogo | Cancelamento e continuidade da solicitação conferidos. Em 320 × 430, janela contida entre 16 e 414 px, com texto rolável e botões de 44 px alcançáveis |
| Perfis | Administrador autenticado com código de acesso local. Representante redirecionado da administração para a agenda; APIs administrativas retornaram 403 |
| Dashboard | Arquivo sem diferenças em relação à base ARENA-84 |

## Evidências

- [Administração antes](arena-85/admin-320-antes.jpg), [cartões mobile](arena-85/admin-cartoes-320.jpg) e [tabela desktop](arena-85/admin-1440-depois.jpg).
- [Validação da pesquisa](arena-85/admin-busca-validacao-320.jpg), [segunda página](arena-85/admin-pagina2-320.jpg) e [lista vazia](arena-85/admin-vazio-320.jpg).
- [Edição de usuário](arena-85/usuario-edicao-320.jpg), [erro de data](arena-85/usuario-erro-320.jpg) e [sucesso](arena-85/usuario-sucesso-320.jpg).
- [Parâmetros mobile](arena-85/parametros-320.jpg), [desktop](arena-85/parametros-desktop.jpg), [validação](arena-85/parametro-erro-320.jpg) e [sucesso](arena-85/parametro-sucesso-320.jpg).
- [Suporte antes](arena-85/suporte-320-antes.jpg), [suporte atual](arena-85/suporte-320-depois.jpg), [validação](arena-85/suporte-validacao-320.jpg), [rascunho](arena-85/suporte-rascunho-320.jpg) e [envio confirmado](arena-85/suporte-sucesso-320.jpg).
- [Orientação de exclusão](arena-85/exclusao-320.jpg) e [altura reduzida](arena-85/exclusao-altura-reduzida.jpg).
- [Matriz responsiva](arena-85/matriz-responsiva.json) e [permissões da API](arena-85/permissoes.json).

## Homologação pendente

Conferência física em Android/WebView, teclado real, seleção de opções por toque no aparelho, áreas seguras e ampliação real de texto/zoom. A matriz foi emulada no navegador integrado. A história permanece Em andamento para homologação, conforme as demais entregas do épico.

## Reprodução

Com Java 21 e banco de integração PostgreSQL configurado nas variáveis `ARENA67_TEST_DB_URL`, `ARENA67_TEST_DB_USER` e `ARENA67_TEST_DB_PASSWORD`:

```powershell
.\mvnw.cmd -q verify
node --test src/test/js/*.test.cjs
git diff --check
```
