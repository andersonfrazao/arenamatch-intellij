CREATE SEQUENCE partida_liga_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE partida_liga (
    id BIGINT DEFAULT nextval('partida_liga_id_seq') PRIMARY KEY,
    partida_id BIGINT NOT NULL,
    liga_id BIGINT NOT NULL,
    data_vinculo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conta_ranking_liga BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_partida_liga_partida FOREIGN KEY (partida_id) REFERENCES partida(id),
    CONSTRAINT fk_partida_liga_liga FOREIGN KEY (liga_id) REFERENCES liga(id),
    CONSTRAINT uk_partida_liga_partida UNIQUE (partida_id)
);
