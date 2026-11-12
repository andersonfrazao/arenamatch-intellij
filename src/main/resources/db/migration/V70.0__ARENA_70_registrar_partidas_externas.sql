CREATE SEQUENCE seq_adversario_externo START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_partida_externa START WITH 1 INCREMENT BY 1;

CREATE TABLE adversario_externo (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_adversario_externo'),
    id_time BIGINT NOT NULL REFERENCES time(id),
    nome VARCHAR(120) NOT NULL CHECK (btrim(nome) <> ''),
    nome_normalizado VARCHAR(120) NOT NULL,
    UNIQUE (id_time, nome_normalizado),
    UNIQUE (id, id_time)
);

CREATE TABLE partida_externa (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_partida_externa'),
    id_time BIGINT NOT NULL REFERENCES time(id),
    id_adversario BIGINT NOT NULL,
    nome_adversario VARCHAR(120) NOT NULL CHECK (btrim(nome_adversario) <> ''),
    modalidade VARCHAR(40) NOT NULL,
    categoria VARCHAR(40) NOT NULL,
    mando VARCHAR(20) NOT NULL CHECK (mando IN ('MANDANTE', 'VISITANTE')),
    data_hora_inicio TIMESTAMP NOT NULL,
    data_hora_fim TIMESTAMP NOT NULL CHECK (data_hora_fim > data_hora_inicio),
    nome_campo VARCHAR(160) NOT NULL CHECK (btrim(nome_campo) <> ''),
    endereco VARCHAR(300),
    valor_taxa NUMERIC(12, 2) NOT NULL CHECK (valor_taxa >= 0),
    observacoes VARCHAR(1000),
    origem VARCHAR(20) NOT NULL DEFAULT 'MANUAL' CHECK (origem = 'MANUAL'),
    chave_solicitacao UUID NOT NULL,
    id_autor BIGINT NOT NULL REFERENCES usuario(id),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_time, chave_solicitacao),
    FOREIGN KEY (id_adversario, id_time) REFERENCES adversario_externo(id, id_time),
    FOREIGN KEY (id_time, modalidade) REFERENCES time_modalidade(id_time, modalidade),
    CHECK (categoria IN ('ESPORTE', 'MESCLADO', 'VETERANO_35', 'VETERANO_40', 'MASTER', 'SUB20'))
);

CREATE INDEX idx_partida_externa_agenda ON partida_externa(id_time, data_hora_inicio);
CREATE INDEX idx_partida_externa_modalidade ON partida_externa(id_time, modalidade, data_hora_inicio);
CREATE TRIGGER modalidade_partida_externa_imutavel
    BEFORE UPDATE OF modalidade ON partida_externa
    FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_modalidade_partida();
