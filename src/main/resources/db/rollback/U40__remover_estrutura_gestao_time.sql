-- Script de contingencia da V40. Nao e executado automaticamente pelo Flyway.
-- Executar somente apos reverter a aplicacao para uma versao que nao use estas tabelas.
DROP TABLE IF EXISTS evento_sumula;
DROP TABLE IF EXISTS participacao_partida;
DROP TABLE IF EXISTS gestao_partida;
DROP TABLE IF EXISTS atleta;

DROP SEQUENCE IF EXISTS seq_evento_sumula;
DROP SEQUENCE IF EXISTS seq_participacao_partida;
DROP SEQUENCE IF EXISTS seq_gestao_partida;
DROP SEQUENCE IF EXISTS seq_atleta;
