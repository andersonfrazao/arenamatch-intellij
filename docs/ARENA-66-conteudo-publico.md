# ARENA-66 — Conteúdo público para encontrar adversários

Jira: https://jcs-tech.atlassian.net/browse/ARENA-66

Branch criada a partir de `main` em `abe797e`. A entrega desta etapa é local, sem merge ou push, aguardando revisão visual do usuário.

## Página

Rota: `/encontrar-times-futebol-amador.xhtml`.

Conteúdo voltado ao representante de um time que procura adversários para amistosos. Inclui o que combinar antes do convite, cadastro e disponibilidade, busca, envio e aceite do desafio, ausência de resultados e acesso pelo navegador ou aplicativo Android. O Básico é descrito como gratuito e com limites, sem prometer adversários disponíveis.

A página complementa o guia `como-funciona.xhtml`. Há links de entrada no menu público (incluindo o menu do login), na página inicial, no guia e no rodapé compartilhado; a nova página aponta para cadastro, login, guia, planos e Google Play.

O menu usa **Funcionalidades** para apresentar os recursos, **Encontrar adversários** para a nova página e **Passo a passo** para o guia de uso. No celular, os links ficam recolhidos em um menu hambúrguer; a ação Entrar fica dentro desse menu e Criar conta permanece visível no cabeçalho. O menu fecha ao selecionar um link, pressionar Escape, clicar fora ou mudar de faixa de largura. Sem JavaScript, os links continuam disponíveis.

O botão de preferências de estatísticas foi movido para o rodapé. Após aceitar ou recusar, o aviso desaparece e não resta botão flutuante cobrindo a página. A escolha pode ser reaberta pelo rodapé; páginas sem rodapé recebem um acesso ao fim do conteúdo. A política de privacidade foi ajustada para indicar esse local.

Foram adicionados título, descrição, canonical e entrada no sitemap. A rota foi incluída na lista exata de páginas institucionais do filtro de autenticação e na lista de páginas públicas do Analytics. O consentimento existente permanece obrigatório para carregar o Google Analytics.

## Revisão local

Inicie a aplicação com sua configuração local habitual e abra:

`http://localhost:8080/encontrar-times-futebol-amador.xhtml`

Se utilizar outra porta ou caminho de contexto, ajuste o endereço. Verifique também o link na seção Como funciona da página inicial e no rodapé.

Para a revisão visual durante a implementação foi usado um servidor temporário em `127.0.0.1:8787`, com as páginas JSF reais e a autenticação pública, sem banco de dados ou envio de Analytics. Ele serve apenas para revisar as páginas públicas; os formulários da aplicação devem ser testados na execução local habitual.

## Verificações

- Testes existentes de acesso público, proteção de rotas internas, sitemap e seleção de páginas do Analytics atualizados para a nova rota.
- `mvn test`: 39 testes aprovados.
- XML das páginas alteradas, do template de rodapé e do sitemap válido.
- Nova página renderizada por JSF com resposta HTTP 200 para visitante anônimo.
- Revisão visual em computador e celular.
- Após a revisão do menu: navegação validada pela página inicial, pelo item Passo a passo e pelo menu expansível do login na aplicação local. Conferidas larguras de 1280, 900, 390 e 320 pixels, sem rolagem horizontal.
- Refinamento para celular: menu hambúrguer, fechamento por Escape e navegação conferidos no navegador. Cabeçalho recolhido com cerca de 77 pixels de altura em 390 pixels de largura.
- `node --test src/test/js/analytics.test.cjs`: 17 testes aprovados, incluindo acesso às preferências pelo rodapé, aceite, recusa e revogação.
- Visual de cookies conferido em prévia temporária com a tag Google substituída por um arquivo local inerte e conexões externas bloqueadas. Nenhum evento real foi enviado durante essa verificação.

Nenhuma campanha Google Ads ou publicação em produção faz parte desta tarefa. O conteúdo é uma hipótese editorial alinhada às funções do produto, sem estimativa validada de volume de buscas ou garantia de classificação nos resultados.
