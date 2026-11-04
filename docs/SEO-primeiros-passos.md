# Arena Match: primeira configuracao de busca

## Preparacao no projeto

- As paginas de apresentacao, guia e privacidade declaram seus proprios enderecos
  canonicos em `https://www.arenamatch.app.br`.
- A raiz continua seguindo a configuracao existente de redirecionamento.
- `sitemap.xml` lista somente essas tres paginas publicas. Login, cadastro e
  paginas privadas nao fazem parte dele.
- `robots.txt` permite o rastreamento e informa o endereco do sitemap. A regra
  geral tambem se aplica ao OAI-SearchBot. Nenhum acesso autenticado foi liberado.
- O filtro libera os dois arquivos apenas nos caminhos exatos da raiz.

## Depois de publicar o site

1. Em uma janela anonima, abrir:
   - `https://www.arenamatch.app.br/robots.txt`
   - `https://www.arenamatch.app.br/sitemap.xml`
2. Conferir que os arquivos aparecem sem redirecionamento ao login. Se a
   hospedagem ou um proxy tratar essas rotas separadamente, ajustar sua entrega.
3. No Search Console, selecionar a propriedade `arenamatch.app.br`.
4. Abrir **Sitemaps**, informar o endereco completo
   `https://www.arenamatch.app.br/sitemap.xml` e clicar em **Enviar**.
5. Na inspecao de URL, testar a pagina `https://www.arenamatch.app.br/index.xhtml`
   e conferir, no HTML da pagina testada, a indicacao `rel="canonical"`.
6. Solicitar a indexacao dessa pagina e de `como-funciona.xhtml`.
7. Acompanhar os relatorios nos dias seguintes. O teste ao vivo nao determina
   qual canonica o Google escolhera. As mudancas nao garantem indexacao,
   posicionamento nem citacoes em respostas do ChatGPT.

Os ajustes so chegam ao site publico depois da publicacao da aplicacao web;
nao exigem uma nova versao do aplicativo Flutter.

## Validacao local

Executar `mvn test`. Os testes de paginas publicas verificam os arquivos de
descoberta, seus tipos de conteudo, acesso anonimo e protecao de caminhos internos.
