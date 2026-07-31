CREATE SEQUENCE publicacao_liga_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE publicacao_liga (
    id BIGINT DEFAULT nextval('publicacao_liga_id_seq') PRIMARY KEY,
    liga_id BIGINT NOT NULL,
    time_autor_id BIGINT NOT NULL,
    data_jogo TIMESTAMP NOT NULL,
    hora_inicio VARCHAR(5),
    hora_fim VARCHAR(5),
    tipo_procura VARCHAR(20) NOT NULL,
    categoria VARCHAR(50),
    regiao VARCHAR(120),
    observacao VARCHAR(500),
    data_expiracao TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_publicacao_liga_liga FOREIGN KEY (liga_id) REFERENCES liga(id),
    CONSTRAINT fk_publicacao_liga_time_autor FOREIGN KEY (time_autor_id) REFERENCES time(id)
);

CREATE INDEX idx_publicacao_liga_liga_status ON publicacao_liga(liga_id, status);
