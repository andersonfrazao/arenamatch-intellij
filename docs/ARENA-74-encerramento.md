# ARENA-74 — Encerramento da migração mobile first

## Decisão de encerramento — 18/11/2026

Por solicitação explícita do usuário, o épico ARENA-74 e suas 12 histórias vinculadas (ARENA-75 a ARENA-86) foram movidos para Concluído no Jira.

Foram implementadas dez histórias. A adaptação específica de Ligas (ARENA-83) e a etapa de homologação integrada pelo Codex (ARENA-86) foram dispensadas. O dashboard também foi excluído do escopo; `dashboard.xhtml` permanece sem alterações em relação à base da migração.

O usuário realizará a homologação manual. Esta decisão substitui as orientações anteriores de manter as histórias Em andamento até a conferência física. Os documentos individuais preservam o histórico dos testes, evidências e limitações observadas em cada entrega. O encerramento no Jira não representa uma declaração de aprovação da homologação física.

## Entregas locais

| História | Entrega | Commit |
| --- | --- | --- |
| ARENA-75 | Base visual e componentes compartilhados | `8d5b0c9` |
| ARENA-76 | Layout principal, navegação e notificações | `7e00d0b` |
| ARENA-77 | Login, ativação e recuperação de senha | `25bc2f3` |
| ARENA-78 | Cadastro e edição do time | `bb1d754` |
| ARENA-79 | Busca de adversários e desafios | `3b0fcd9` |
| ARENA-80 | Agenda, partidas, placares e flyer | `34aed99` |
| ARENA-81 | Chat de partidas e viewport | `1405896` |
| ARENA-82 | Ranking, Meu Scout e adversários | `c479e2a` |
| ARENA-83 | Dispensada: adaptação específica de Ligas | — |
| ARENA-84 | Páginas públicas, guia e consentimento | `8bcf8a8` |
| ARENA-85 | Administração e suporte; dashboard excluído | `1f5e151` |
| ARENA-86 | Dispensada: homologação será manual pelo usuário | — |

A branch ARENA-85 contém a sequência completa das entregas, baseada na main `8b8720a`. Os commits permanecem locais, sem push. Este registro não altera nem publica a aplicação e não realiza merge na main.

## Evidências e testes

Os relatórios individuais estão em `docs/ARENA-75-mobile-first.md` até `docs/ARENA-85-mobile-first.md`, exceto ARENA-83, com capturas e medições nas respectivas pastas `docs/arena-*`.

Na última entrega, o Maven Wrapper `verify` aprovou 123 testes Java, sem falhas, erros ou ignorados. A suíte JavaScript aprovou 71 testes. Esses são os resultados da implementação registrada na ARENA-85; não foi executada uma nova rodada de homologação integrada da ARENA-86.

A conferência física em Android/WebView, teclado real, seleção por toque no aparelho, áreas seguras e ampliação real de texto/zoom fica a cargo do usuário, conforme a decisão acima.
