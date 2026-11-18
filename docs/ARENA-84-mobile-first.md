# ARENA-84 — Páginas públicas, guia e consentimento

## Base e escopo

Branch ARENA-84 criada a partir da main atualizada (`8b8720a`), incorporando as dependências locais até ARENA-82 (`c479e2a`). Esta é a nona história da sequência vigente de 11 histórias do épico ARENA-74. Ligas estão dispensadas. O dashboard também foi excluído por orientação do usuário; administração e suporte permanecem no escopo futuro da ARENA-85.

## Implementação

- Os estilos de apresentação, guia e amistosos passam a começar com uma coluna, índice no fluxo da página e cartões empilhados. As grades se ampliam a partir de 60 rem. O menu completo aparece a partir de 75 rem, mantendo o botão de abertura nas larguras intermediárias.
- A apresentação preserva a identidade dark, cores, textos comerciais, preços, modalidades e destinos existentes de cadastro, login e Google Play.
- A altura real do cabeçalho alimenta o espaçamento das âncoras e dos índices laterais. Ao selecionar uma seção no menu da própria página, o foco passa para seu conteúdo. Escape fecha o menu e devolve o foco ao botão.
- Links, FAQ, botões e índices recebem áreas de toque de pelo menos 44 px de altura. Conteúdo e grades permitem quebra de texto sem alargar a página.
- As cinco imagens do guia foram substituídas por capturas reais das telas revisadas: responsável, time/local, agenda com modalidades, busca com filtro obrigatório e confirmação do desafio com modalidade. Os dados são fictícios e foram capturados em uma base local isolada. A imagem da agenda na apresentação também foi atualizada.
- As imagens têm dimensões declaradas correspondentes aos arquivos, largura limitada pelo conteúdo e links com nomes distintos para abrir a captura completa. As legendas deixam de indicar versões anteriores às modalidades.
- A apresentação da privacidade foi extraída do bloco incorporado para `css/privacidade.css`, com base compacta e ampliação progressiva. O texto dos termos não foi alterado.
- O banner de analytics empilha ações no celular, usa altura limitada à janela com rolagem própria e considera as áreas seguras. Após aceitar ou recusar, o foco volta para o botão visível de preferências. Reabrir as preferências direciona o foco para Aceitar estatísticas.
- A autorização de analytics por configuração, domínio e HTTPS permanece no script de produção. Não foram acrescentados novos eventos de conversão; a configuração de medição existente foi preservada.

Não houve alterações de banco, autenticação, cadastro de usuários ou destino inicial da raiz. A entrada do aplicativo continua seguindo a configuração existente.

## Verificação em 18/11/2026

| Verificação | Resultado |
| --- | --- |
| Build | Maven Wrapper `verify` aprovado; empacotamento final aprovado |
| Java | 123 testes aprovados, sem falhas, erros ou ignorados |
| JavaScript | 69 testes aprovados, incluindo nova regressão de foco após aceitar/recusar. Sintaxe dos dois scripts conferida |
| Matriz responsiva | 45 combinações: quatro páginas públicas e banner em nove dimensões, sem rolagem horizontal da página |
| Dimensões CSS | 320 × 780, 360 × 780, 390 × 844, 768 × 1024, 992 × 900, 1024 × 900, 1440 × 900, 844 × 390 e 320 × 430 |
| Áreas de toque | Ajustes adicionais no contato, marca do rodapé e índice do guia. Medições finais em 320, 992 e 1440 px confirmam altura mínima de 44 px nesses controles |
| Menu e âncoras | Abertura, Escape e navegação para Planos exercitados. Foco final em `planos`; início da seção em 93,45 px, abaixo do cabeçalho de 77 px |
| FAQ | Expansão da pergunta sobre modalidades pelo teclado |
| Guia | Links e dimensões das cinco imagens conferidos; abertura da captura de modalidades e retorno ao guia exercitados |
| Conteúdo e metadados | Um H1 e canonical correspondente em cada página. Títulos, quatro modalidades, planos e destinos dos links preservados |
| Consentimento | Aceitar, recusar, reabrir preferências e retorno de foco exercitados. Botões com altura mínima de 44 px; banner contido na janela inclusive em 320 × 430 e paisagem |

O banner foi verificado em um ensaio visual temporário local em `localhost:18078`, usando o CSS e o script da aplicação. Somente nesse ensaio foram adaptados o domínio/protocolo de ativação e o destino da tag para um arquivo local vazio, impedindo envio ao Google. Nenhuma dessas adaptações foi incluída no código de produção. Os testes automatizados continuam verificando o bloqueio de medição fora do domínio/HTTPS permitido, aceite, recusa, revogação e limpeza das URLs. A aplicação de prévia em `localhost:18077` manteve analytics desativado.

Os testes da página pública verificam os destinos da raiz e a disponibilidade de páginas, sitemap e recursos públicos. A conferência dos links externos foi feita pelos destinos publicados; não houve alteração na conta da Play Store nem medição no Google Analytics de produção.

## Evidências

- [Início antes](arena-84/inicio-320-antes.jpg), [início mobile](arena-84/inicio-320-depois.jpg) e [desktop](arena-84/inicio-1440-depois.jpg).
- [Menu mobile](arena-84/menu-320.jpg), [planos por âncora](arena-84/planos-320.jpg) e [FAQ](arena-84/faq-320.jpg).
- [Guia antes](arena-84/guia-320-antes.jpg), [guia mobile](arena-84/guia-320-depois.jpg) e [guia desktop](arena-84/guia-1440.jpg).
- [Amistosos](arena-84/amistosos-320.jpg), [privacidade](arena-84/privacidade-320.jpg), [consentimento](arena-84/consentimento-320.jpg) e [consentimento em paisagem](arena-84/consentimento-paisagem.jpg).
- [Matriz responsiva](arena-84/matriz-responsiva.json) e [medições após os ajustes finais](arena-84/ajustes-finais.json). A matriz registra as áreas menores identificadas antes dos últimos ajustes; o segundo arquivo registra a correção desses controles.

As capturas que integram o produto estão em `src/main/resources/META-INF/resources/images/guia/*-mobile.jpg` e `images/arena-match-agenda-mobile.jpg`.

## Homologação pendente

Conferência física em Android/WebView, áreas seguras e ampliação real de texto/zoom. A matriz foi emulada no navegador integrado. A história permanece Em andamento para homologação, conforme as demais entregas do épico.

## Reprodução

Com Java 21 e banco de integração PostgreSQL configurado nas variáveis `ARENA67_TEST_DB_URL`, `ARENA67_TEST_DB_USER` e `ARENA67_TEST_DB_PASSWORD`:

```powershell
.\mvnw.cmd -q verify
node --test src/test/js/*.test.cjs
git diff --check
```
