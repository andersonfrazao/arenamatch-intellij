CREATE SEQUENCE seq_atleta START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_gestao_partida START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_participacao_partida START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_evento_sumula START WITH 1 INCREMENT BY 1;

CREATE TABLE atleta (
    id BIGINT DEFAULT nextval('seq_atleta') PRIMARY KEY,
    id_time BIGINT NOT NULL REFERENCES time(id),
    nome VARCHAR(150) NOT NULL,
    apelido VARCHAR(80),
    situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_atleta_situacao CHECK (situacao IN ('ATIVO', 'INATIVO'))
);

CREATE INDEX idx_atleta_time_situacao ON atleta (id_time, situacao);
CREATE INDEX idx_atleta_nome ON atleta (id_time, nome);

CREATE TABLE gestao_partida (
    id BIGINT DEFAULT nextval('seq_gestao_partida') PRIMARY KEY,
    id_partida BIGINT NOT NULL REFERENCES partida(id),
    id_time BIGINT NOT NULL REFERENCES time(id),
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    etapa VARCHAR(30) NOT NULL DEFAULT 'ESCALACAO',
    formacao VARCHAR(30),
    formacao_personalizada VARCHAR(60),
    id_criado_por BIGINT REFERENCES usuario(id),
    id_alterado_por BIGINT REFERENCES usuario(id),
    id_publicado_por BIGINT REFERENCES usuario(id),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_publicacao TIMESTAMP,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_gestao_partida_time UNIQUE (id_partida, id_time),
    CONSTRAINT ck_gestao_status CHECK (status IN ('RASCUNHO', 'PENDENTE_CONCLUSAO', 'PUBLICADO')),
    CONSTRAINT ck_gestao_etapa CHECK (etapa IN ('ESCALACAO', 'OCORRENCIAS', 'REVISAO', 'PUBLICACAO'))
);

CREATE INDEX idx_gestao_time_status ON gestao_partida (id_time, status);
CREATE INDEX idx_gestao_partida ON gestao_partida (id_partida);
CREATE INDEX idx_gestao_alteracao ON gestao_partida (id_time, data_alteracao DESC);

CREATE TABLE participacao_partida (
    id BIGINT DEFAULT nextval('seq_participacao_partida') PRIMARY KEY,
    id_gestao_partida BIGINT NOT NULL REFERENCES gestao_partida(id) ON DELETE CASCADE,
    id_atleta BIGINT NOT NULL REFERENCES atleta(id),
    papel VARCHAR(30) NOT NULL,
    numero_camisa INTEGER,
    posicao VARCHAR(80),
    slot_tatico VARCHAR(40),
    coordenada_x NUMERIC(5,2),
    coordenada_y NUMERIC(5,2),
    ordem INTEGER NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_participacao_gestao_atleta UNIQUE (id_gestao_partida, id_atleta)
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uk_participacao_slot_tatico UNIQUE (id_gestao_partida, slot_tatico)
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT ck_participacao_papel CHECK (papel IN ('TITULAR', 'RESERVA', 'RELACIONADO')),
    CONSTRAINT ck_participacao_camisa CHECK (numero_camisa IS NULL OR numero_camisa BETWEEN 1 AND 99),
    CONSTRAINT ck_participacao_x CHECK (coordenada_x IS NULL OR coordenada_x BETWEEN 0 AND 100),
    CONSTRAINT ck_participacao_y CHECK (coordenada_y IS NULL OR coordenada_y BETWEEN 0 AND 100)
);

CREATE INDEX idx_participacao_atleta ON participacao_partida (id_atleta);
CREATE INDEX idx_participacao_gestao_papel ON participacao_partida (id_gestao_partida, papel);

CREATE TABLE evento_sumula (
    id BIGINT DEFAULT nextval('seq_evento_sumula') PRIMARY KEY,
    id_gestao_partida BIGINT NOT NULL REFERENCES gestao_partida(id) ON DELETE CASCADE,
    id_participacao BIGINT REFERENCES participacao_partida(id) ON DELETE CASCADE,
    id_adversario BIGINT REFERENCES time(id),
    tipo VARCHAR(30) NOT NULL,
    minuto INTEGER,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_evento_tipo CHECK (tipo IN ('GOL', 'CARTAO_AMARELO', 'CARTAO_VERMELHO', 'GOL_CONTRA')),
    CONSTRAINT ck_evento_minuto CHECK (minuto IS NULL OR minuto BETWEEN 0 AND 200),
    CONSTRAINT ck_evento_origem CHECK (
        (tipo = 'GOL_CONTRA' AND id_participacao IS NULL AND id_adversario IS NOT NULL)
        OR
        (tipo <> 'GOL_CONTRA' AND id_participacao IS NOT NULL AND id_adversario IS NULL)
    )
);

CREATE INDEX idx_evento_gestao_tipo ON evento_sumula (id_gestao_partida, tipo);
CREATE INDEX idx_evento_participacao ON evento_sumula (id_participacao);
