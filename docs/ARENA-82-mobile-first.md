# ARENA-82 — Ranking, Meu Scout e adversários

> Atualização de homologação em 19/11/2026: o ranking em cartões descrito na entrega original foi substituído por tabela compacta. A seção final registra a apresentação vigente e suas evidências.

## Base da entrega

A branch ARENA-82 parte da main atualizada em `8b8720a`, com as dependências locais até ARENA-81 incorporadas. A base imediata é `1405896`. Esta é a oitava história da sequência vigente de 11 histórias do épico ARENA-74.

## Implementação

- No celular, cada linha do ranking se apresenta como cartão: posição, nome completo e sete estatísticas. O mesmo componente PrimeFaces oferece tabela comparativa a partir de 768 px. A rolagem horizontal, quando necessária, pertence ao componente e apresenta orientação visual e associação acessível.
- Filtro de modalidade e abas têm apresentação mobile como base. As abas identificam seleção e painel associado; setas, Home e End permitem alternar pelo teclado. A atualização parcial mantém o foco no destino correspondente.
- Meu Scout apresenta indicadores em uma lista de definições com duas colunas na base, ampliada progressivamente. O histórico aceita nomes extensos, preserva data e horário e apresenta placares sem truncar os times.
- O botão Carregar mais mantém os resultados existentes e direciona o foco ao primeiro jogo acrescentado. Após uma falha, a nova tentativa consulta a mesma página; o índice avança somente quando o carregamento termina com sucesso.
- Detalhes do adversário reutiliza cartões, métricas e histórico. Escudo, nome, modalidade, informações, confronto direto e resultados se ajustam à largura disponível. O retorno à busca mantém data, categoria e modalidade.
- Preparar, cancelar e enviar desafio preservam o fluxo existente. A mensagem opcional tem rótulo associado e limite de 500 caracteres. A resposta aparece dentro do formulário.
- Estados de carregamento, indisponibilidade, acesso restrito, ausência de jogos e falha de atualização possuem apresentação própria. Ranking, Scout e detalhes oferecem nova tentativa quando aplicável. Uma falha de consulta ao mudar a modalidade limpa o ranking anterior.
- `css/desempenho.css` concentra os estilos com escopo `.am-desempenho`, usando a identidade dark/glass e os tokens compartilhados. As ampliações usam consultas `min-width`. `js/desempenho.js` cuida do foco, das abas e do aviso de rolagem.

As regras de classificação, partidas confirmadas e autorização permanecem no backend existente. Esta história não altera banco de dados nem acrescenta migrations.

## Validação em 18/11/2026

As verificações usam somente contas, times e partidas fictícias em PostgreSQL 15 local isolado. O banco da interface e o de integração são separados. A prévia foi executada em `http://localhost:18077`.

| Verificação | Resultado |
| --- | --- |
| Build e Java | Maven Wrapper `verify` aprovado: 123 testes, sem falhas, erros ou testes ignorados. Empacotamento final aprovado após os ajustes de CSS/JavaScript |
| JavaScript existente | 68 testes aprovados |
| Matriz responsiva | 27 combinações de ranking, Scout e detalhes, sem largura da página superior ao viewport |
| Dimensões CSS | 320 × 780, 360 × 780, 390 × 844, 768 × 1024, 992 × 900, 1024 × 900, 1440 × 900, 844 × 390 e 320 × 430 |
| Conteúdo | Nove células por time no ranking, sete métricas no Scout e 14 métricas nos detalhes, mantidas nas combinações medidas |
| Ajustes finais | Seletor medido em 44 px de altura; abas em pelo menos 44 × 44 px. Fechamento da mensagem de erro medido em 44 × 44 px. Cabeçalhos da tabela desktop visíveis após atualização da revisão dos recursos |
| Modalidades | Campo: três equipes com partidas confirmadas. Futsal: duas equipes. Fut 7: estado sem equipes com jogos confirmados |
| Exclusão de resultados pendentes | Partidas fictícias com placares 777 e 999, sem confirmação, não integram as métricas nem incluem a equipe sem partidas confirmadas no ranking |
| Scout e paginação | Campo: 12 jogos, inicialmente dez e mais dois após Carregar mais, com foco no primeiro acrescentado. Futsal: três jogos após trocar a modalidade |
| Planos pela API | Trial, PRO, PREMIUM e administrador com plano Básico têm acesso. Básico comum recebe 403 para Campo e Futsal |
| Apresentação dos planos | Trial com dados; Básico com bloqueio no ranking e nos detalhes; PREMIUM com indicadores zerados e histórico vazio |
| Desafio | Preparação com foco no campo opcional, envio com confirmação visual e cancelamento exercitados pela interface |
| Erro e nova tentativa | Adversário inexistente apresenta erro dentro do formulário e botão Tentar novamente. A repetição mantém a mensagem e leva o foco a ela |
| Teclado | Alternância das abas por Home e seta; seleção de modalidade por setas e Enter; foco no painel atualizado e nos novos jogos |

Os cinco novos testes Java cobrem falha ao carregar mais sem pular página, repetição da primeira página, reinício do histórico por modalidade, limpeza de classificação antiga após erro e ausência de consultas restritas para o Básico.

## Homologação pendente

A conferência física em Android/WebView permanece pendente, incluindo teclado real, áreas seguras, orientação e ampliação real de texto/zoom. Os tamanhos e a orientação foram emulados no navegador integrado. A história segue Em andamento para homologação, conforme as entregas anteriores do épico.

## Evidências

As capturas usam dados fictícios. As imagens de antes correspondem à base ARENA-81.

- [Ranking antes](arena-82/ranking-320-antes.jpg), [ranking mobile](arena-82/ranking-320-depois.jpg), [cartões](arena-82/ranking-cartoes-320.jpg) e [tabela desktop](arena-82/ranking-1440-depois.jpg).
- [Meu Scout](arena-82/scout-320-depois.jpg), [Scout PREMIUM sem jogos](arena-82/scout-premium-vazio-320.jpg) e [ranking sem jogos](arena-82/ranking-vazio.jpg).
- [Adversário antes](arena-82/adversario-320-antes.jpg), [detalhes mobile](arena-82/adversario-320-depois.jpg) e [detalhes desktop](arena-82/adversario-1440-depois.jpg).
- [Preparação do desafio](arena-82/desafio-320.jpg), [envio confirmado](arena-82/desafio-sucesso-320.jpg) e [erro com nova tentativa](arena-82/adversario-erro-320.jpg).
- [Ranking Básico](arena-82/ranking-basico-320.jpg) e [detalhes bloqueados](arena-82/adversario-basico-320.jpg).
- [Matriz responsiva](arena-82/matriz-responsiva.json), [áreas de toque após os ajustes](arena-82/ajustes-finais.json) e [resultados por plano e modalidade](arena-82/permissoes-modalidades.json).

## Reprodução dos testes

Usar Java 21 e PostgreSQL local com o banco de integração configurado nas variáveis `ARENA67_TEST_DB_URL`, `ARENA67_TEST_DB_USER` e `ARENA67_TEST_DB_PASSWORD`.

```powershell
.\mvnw.cmd -q verify
node --test src/test/js/*.test.cjs
git diff --check
```

## Correção visual da homologação — 19/11/2026

Após a comparação apresentada pelo usuário com a versão Claude, a ARENA-85 passa a mostrar o ranking em tabela comparativa em todas as larguras. Cada time ocupa uma linha, com posição, nome e colunas V/J/E/D/GP/GS/SG alinhadas. Os três primeiros recebem medalhas, o próprio time recebe faixa azul e identificação Seu time, e uma legenda expansível explica as abreviações.

O filtro foi simplificado para Modalidade, o contexto da modalidade aparece no cabeçalho do ranking e os contêineres repetidos foram reduzidos. Em 480 × 960, as seis equipes demonstrativas aparecem na mesma tela. Nas larguras verificadas a partir de 390 px, a tabela coube sem rolagem horizontal com os dados de teste. Em 320 e 360 px, a rolagem fica restrita ao componente, com orientação visível e nome do time fixo durante o deslocamento.

A validação utilizou somente dados fictícios no PostgreSQL isolado e prévia na porta 18077, preservando as aplicações do usuário em 8080/8081. Foram conferidos 320, 360, 390, 430, 480, 768 e 1440 px, com seis linhas, nove células por linha, três medalhas e uma única identificação do próprio time. Não houve largura excedente da página nem estatísticas cortadas. Nome longo e números de três dígitos também foram exercitados.

Foram verificados a legenda pelo teclado, a troca para Futsal, a modalidade sem jogos e o Meu Scout, incluindo carregamento de mais jogos (dez para vinte). Os cinco testes existentes de RankingBean passaram; o empacotamento Maven passou. Não houve alteração Java, de regras de classificação ou de acesso. As regras compartilhadas do Scout e dos detalhes de adversário foram preservadas.

Evidências atuais: [480 px](arena-82/ranking-ajuste-480.jpg), [390 px](arena-82/ranking-ajuste-390.jpg), [rolagem em 320 px](arena-82/ranking-ajuste-rolagem-320.jpg), [legenda](arena-82/ranking-ajuste-legenda.jpg), [nome extenso](arena-82/ranking-ajuste-texto-extenso.jpg) e [medições](arena-82/ranking-ajuste-matriz.json).
