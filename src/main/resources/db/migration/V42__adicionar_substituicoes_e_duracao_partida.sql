-- A duracao e opcional e serve exclusivamente para calcular o tempo jogado.
ALTER TABLE gestao_partida ADD COLUMN duracao_minutos INTEGER;

ALTER TABLE gestao_partida ADD CONSTRAINT ck_gestao_duracao
    CHECK (duracao_minutos IS NULL OR duracao_minutos BETWEEN 1 AND 300);

-- Somente as substituicoes possuem minuto. Gols e cartoes continuam sem
-- detalhamento temporal e gols contra nao sao registrados individualmente.
CREATE SEQUENCE seq_substituicao_partida START WITH 1 INCREMENT BY 1;

CREATE TABLE substituicao_partida (
    id BIGINT DEFAULT nextval('seq_substituicao_partida') PRIMARY KEY,
    id_gestao_partida BIGINT NOT NULL REFERENCES gestao_partida(id) ON DELETE CASCADE,
    id_participacao_saiu BIGINT NOT NULL REFERENCES participacao_partida(id) ON DELETE CASCADE,
    id_participacao_entrou BIGINT NOT NULL REFERENCES participacao_partida(id) ON DELETE CASCADE,
    minuto INTEGER,
    ordem INTEGER NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_substituicao_gestao_ordem UNIQUE (id_gestao_partida, ordem)
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT ck_substituicao_atletas CHECK (id_participacao_saiu <> id_participacao_entrou),
    CONSTRAINT ck_substituicao_minuto CHECK (minuto IS NULL OR minuto BETWEEN 0 AND 300),
    CONSTRAINT ck_substituicao_ordem CHECK (ordem >= 0)
);

CREATE INDEX idx_substituicao_gestao ON substituicao_partida (id_gestao_partida, ordem);
CREATE INDEX idx_substituicao_saiu ON substituicao_partida (id_participacao_saiu);
CREATE INDEX idx_substituicao_entrou ON substituicao_partida (id_participacao_entrou);
