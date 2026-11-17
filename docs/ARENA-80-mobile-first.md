# ARENA-80 — Agenda, placares e flyer

## Base da entrega

A branch ARENA-80 parte da main atualizada em `8b8720a`, com as dependências locais até a ARENA-79 incorporadas. A base imediata é `3b0fcd9`. Esta entrega corresponde à sexta história da sequência vigente de 11 histórias do épico ARENA-74.

## Implementação

- `css/agenda.css` concentra a apresentação com escopo `.am-agenda` e `.agenda-flyer`. O calendário começa com três colunas, passa a cinco em 576 px e a sete em 1200 px. As legendas acompanham a largura disponível.
- Os times aparecem em linhas próprias no celular, com escudo e nome completo. Os detalhes passam a apresentar a modalidade também nos jogos confirmados. Local, taxa e horário recebem quebra de linha conforme a largura disponível.
- A lista Todos meus jogos usa um botão HTML com estado expandido, acionamento por teclado e fechamento por Escape. A expansão é preservada nas atualizações do formulário. Selecionar um jogo fecha a lista e leva o foco aos detalhes.
- Os campos de gols possuem rótulos associados, entrada numérica e apresentação em uma coluna no celular. A confirmação do resultado e o placar final também se reorganizam para leitura em largura pequena.
- Salvar placar, propor outro resultado e enviar motivo de cancelamento processam os campos do cartão correspondente. As demais ações processam o próprio comando.
- As mensagens ficam no formulário e recebem foco quando há erro. O estado Processando acompanha as ações. A atualização das mensagens fica vinculada aos comandos da agenda, permitindo que o aviso permaneça durante a atualização do sininho.
- O diálogo de flyer acompanha largura e altura disponíveis, com conteúdo rolável e ações no rodapé. O compartilhamento aparece conforme o suporte do navegador a arquivos. A imagem exportada permanece PNG de 1080 × 1350 px.
- A geração apresenta andamento e erros no diálogo, permite repetir após falha e descarta respostas de gerações encerradas. Fechar libera a imagem temporária e devolve o foco ao botão Gerar flyer. Os nomes usam até quatro linhas na arte exportada.

## Correções no fluxo

O carregamento do calendário preserva a data selecionada quando ela pertence ao período atualizado. A mudança para outra data limpa os rascunhos de placar e cancelamento. O botão Hoje seleciona explicitamente a data atual.

A agenda passa a fornecer o comando `atualizarCalendarioAjax`, já esperado pelo fluxo de ações das notificações. A contestação também usa o seletor compartilhado para atualizar o painel de notificações.

## Validação em 17/11/2026

Os cenários usam contas, equipes e partidas fictícias no PostgreSQL 15 local. A interface usa um banco separado do banco dos testes de integração. A prévia foi executada em `http://localhost:18077`.

| Verificação | Resultado |
| --- | --- |
| Maven Wrapper, verify | 108 testes aprovados, com zero falhas, erros ou testes ignorados |
| JavaScript, node --test | 55 testes aprovados |
| Matriz responsiva | 18 combinações de agenda e flyer, com a página contida no viewport |
| Dimensões CSS | 320 × 780, 360 × 780, 390 × 844, 768 × 1024, 992 × 900, 1024 × 900, 1440 × 1000, 844 × 390 e 320 × 430 |
| Áreas de toque | Controles visíveis medidos em pelo menos 44 × 44 px nas combinações verificadas |
| Placar pendente | Validação dos dois gols e envio de uma entre duas partidas do mesmo dia, em Campo e Futsal. A outra partida permaneceu pendente |
| Resultado recebido | Confirmação pela agenda, com placar final apresentado e dia selecionado preservado |
| Disputa | Contestação pela agenda e envio de uma nova proposta de placar |
| Convites | Estados recebido, enviado e expirado. Aceite pela agenda em data disponível, com confirmação da partida |
| Regras apresentadas | Mensagens de pendência de placar e conflito de data apareceram no formulário durante tentativas de aceite |
| Cancelamento | Recusa de pedido, abertura do motivo com foco, motivo obrigatório e envio de solicitação |
| Notificações | Aceite de cancelamento pelo sininho atualizou o cartão da agenda e preservou o dia 29 selecionado |
| Calendário | Troca de período, retorno para hoje, seleção de dia e estado vazio |
| Flyer | Download real do PNG, conferência da imagem exportada e retorno do foco ao fechar |
| Altura reduzida | Propor Novo Placar alcançável por Tab acima da navegação inferior em 320 × 430. Rodapé do flyer alcançável em paisagem e altura reduzida |

Os testes novos cobrem seleção e limpeza de rascunhos, estrutura da página, expansão após atualização, foco após erro e navegação, geração, nomes extensos, suporte a compartilhamento, cancelamento voluntário, falha recuperável e respostas tardias ao fechar e gerar novamente.

## Homologação pendente

A conferência física em Android/WebView permanece pendente, incluindo teclado real, áreas seguras e compartilhamento com aplicativos instalados. A ampliação real de texto/zoom integra a homologação final do épico. A história segue Em andamento para homologação.

A adoção pelos jogos manuais da ARENA-70 acompanha a integração futura dessa história à main.

## Evidências

As capturas utilizam dados fictícios. O arquivo de antes registra a base da ARENA-79. As demais capturas registram estados da implementação e a matriz final.

- [Agenda antes](arena-80/agenda-320-antes.jpg), [cabeçalho da agenda](arena-80/agenda-320-depois.jpg) e [dia vazio](arena-80/agenda-vazia-320.jpg).
- [Detalhes em 320 px](arena-80/partida-320.jpg), [tablet](arena-80/partida-768.jpg) e [desktop](arena-80/partida-1440.jpg).
- [Convite e cancelamento](arena-80/convite-recebido-cancelamento-320.jpg), [placar em disputa](arena-80/disputa-320.jpg) e [ação com altura reduzida](arena-80/placar-altura-reduzida.jpg).
- [Flyer no celular](arena-80/flyer-320.jpg), [paisagem](arena-80/flyer-paisagem.jpg), [altura reduzida](arena-80/flyer-altura-reduzida.jpg) e [PNG exportado](arena-80/flyer-1080x1350.png).
- [Medições da matriz responsiva](arena-80/matriz-responsiva.json).

## Reprodução dos testes

Usar Java 21 e PostgreSQL local com o banco de integração configurado nas variáveis `ARENA67_TEST_DB_URL`, `ARENA67_TEST_DB_USER` e `ARENA67_TEST_DB_PASSWORD`.

```powershell
.\mvnw.cmd -q verify
node --test src/test/js/*.test.cjs
git diff --check
```
