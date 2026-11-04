# ARENA-65: Google Analytics nas paginas publicas

O perfil `prod` habilita o ID `G-VPZK0B0PSH`. Fora dele, a integracao fica
desativada por padrao. As propriedades `arenamatch.analytics.enabled` e
`arenamatch.analytics.measurement-id` permitem ajustar ou desligar a integracao.
O JavaScript tambem exige HTTPS e um dos dois hosts de producao do Arena Match.

## Comportamento

- A tag compartilhada aparece somente em index, como-funciona, privacidade,
  login e cadastro, uma vez por pagina. A area autenticada nao e instrumentada.
- O aviso permite aceitar ou recusar estatisticas com botoes equivalentes.
- A biblioteca do Google nao e carregada antes de aceitar. Nao ha pings de
  Analytics antes do aceite inicial. A preferencia fica neste navegador.
- O botao **Cookies de estatisticas** permite reabrir a escolha. Revogar ativa o
  opt-out do destino GA4 e remove cookies `_ga` e `_ga_*` acessiveis, sem
  recarregar a pagina ou apagar cookies de sessao. Dados ja enviados nao sao
  removidos pela revogacao.
- Publicidade e Google Signals ficam desativados. A configuracao de pagina e
  referencia omite query strings e fragmentos. Nenhum valor dos campos dos
  formularios e enviado pela integracao.
- Se o navegador bloquear armazenamento ou Google, o site continua funcionando.
- A medicao otimizada configurada no painel do Google pode registrar interacoes
  apos o aceite. `form_submit` nao comprova cadastro concluido; o evento de
  sucesso do cadastro sera implementado separadamente.

## Validacao local

1. Executar `mvn test` para verificar a configuracao e os testes existentes.
2. Executar `node --test src/test/js/analytics.test.cjs` para testar a coleta
   condicionada ao aceite, recusa, revogacao, URLs e carregamento unico.

## Depois da publicacao manual

1. Abrir a pagina inicial em uma janela anonima. Conferir o aviso em desktop e
   celular, a navegacao por teclado e o link da politica de privacidade.
2. Antes de escolher, conferir na aba Rede que nao existem requisicoes a
   googletagmanager.com ou google-analytics.com provenientes da integracao.
3. Recusar, navegar ao guia e confirmar que a recusa foi mantida.
4. Reabrir **Cookies de estatisticas** e aceitar. Conferir o carregamento da tag
   e os eventos no relatorio **Tempo real** do Google Analytics.
5. Revogar e confirmar o bloqueio de novas medicoes, preservando o uso do site.
6. O teste de instalacao do painel pode nao detectar a tag enquanto nao houver
   aceite. Usar Tag Assistant com a preferencia concedida para conferir o ID.
7. Validar cadastro e login. Nao e necessario publicar um novo AAB para esta
   integracao web. Dados nativos de instalacao do app nao sao medidos aqui.

Os testes automatizados nao enviam eventos reais ao Google. A confirmacao de
coleta em producao depende da publicacao e do painel da conta do responsavel.
