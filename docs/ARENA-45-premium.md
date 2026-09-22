# ARENA-45 — acesso PREMIUM

PREMIUM com status PAGO inclui os recursos PRO e autoriza a organização de campeonatos. TRIAL e PRO não autorizam organização. A escolha do novo plano já aparece na edição administrativa de usuários, que utiliza os valores de `PlanoAssinatura`.

O menu desktop/mobile apresenta Recursos PREMIUM > Campeonatos > Organizar campeonatos, Campeonatos do time e Convites recebidos. A organização fica visível e desabilitada sem acesso; Conhecer o PREMIUM abre a apresentação. Os atalhos de participação exigem um time, não PREMIUM.

## Contratos e continuidade

- `GET /api/campeonatos/acesso`: consulta autenticada; retorna `podeOrganizar` e `mensagem`, sem dados de campeonatos.
- `GET /api/campeonatos/administracao/acesso`: exige PREMIUM/PAGO; retorna 403 sem esse acesso.
- `CampeonatoAuthorizationService.exigirPremium()` consulta o usuário atual no banco. As futuras operações de criação devem chamar esse método.
- As futuras operações sobre campeonatos existentes devem chamar `exigirOrganizador(organizadorId)` com o proprietário obtido do registro no banco, nunca do corpo enviado pelo cliente. PREMIUM não concede acesso aos campeonatos de outros organizadores.
- Participação, convites e inscrição do elenco terão autorização própria nas respectivas histórias. Não aplicar a exigência PREMIUM a essas operações.
- Downgrade apenas bloqueia autorização; não há exclusão de registros nem escrita no cadastro ao consultar permissões. Reativar PREMIUM/PAGO restabelece o acesso.

Nesta história, a página de Campeonatos apresenta o acesso e informa as próximas etapas. Cadastro de competições, envio/resposta a convites e listagens reais serão implementados nas histórias correspondentes; não há operações simuladas nem dados fictícios na aplicação.

## Banco e pagamento

Não é necessária migration: `usuario.plano_assinatura` já é VARCHAR(20), sem restrição de valores nas migrations existentes, e o enum é persistido como texto. Não há alteração automática dos planos existentes. A ativação utiliza a administração atual; checkout ou gateway de pagamento não fazem parte desta entrega. Antes de voltar a uma versão anterior do aplicativo, revisar usuários com PREMIUM, pois versões antigas não reconhecem o novo enum.

## Validação

Testes cobrem combinações de plano/pagamento, herança PRO, autenticação, propriedade do organizador, downgrade/reativação com a mesma autenticação, acesso direto pela API e falha segura da consulta na interface. Validação XML dos templates e prévia de CSS do menu em mobile e desktop. A validação integrada em uma sessão JSF autenticada ainda depende de executar a aplicação.
