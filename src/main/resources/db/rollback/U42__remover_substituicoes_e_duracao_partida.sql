-- Rollback manual da V42 para o ambiente de DEV.
-- Execute este arquivo antes de remover a versao 42 da flyway_schema_history.

BEGIN;

-- A tabela elimina tambem seus indices, constraints e dados de substituicoes.
DROP TABLE IF EXISTS substituicao_partida CASCADE;
DROP SEQUENCE IF EXISTS seq_substituicao_partida;

-- A constraint ck_gestao_duracao e removida junto com a coluna.
ALTER TABLE gestao_partida
    DROP COLUMN IF EXISTS duracao_minutos CASCADE;

-- A primeira versao da V42 alterava este limite para 300.
-- Restaura a definicao original criada pela V40.
ALTER TABLE evento_sumula
    DROP CONSTRAINT IF EXISTS ck_evento_minuto;

ALTER TABLE evento_sumula
    ADD CONSTRAINT ck_evento_minuto
    CHECK (minuto IS NULL OR minuto BETWEEN 0 AND 200);

COMMIT;

-- A limpeza do historico deve ser feita separadamente, depois do rollback:
-- DELETE FROM flyway_schema_history WHERE version = '42';
