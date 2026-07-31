CREATE SEQUENCE banimento_liga_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE banimento_liga (
    id BIGINT DEFAULT nextval('banimento_liga_id_seq') PRIMARY KEY,
    liga_id BIGINT NOT NULL,
    time_banido_id BIGINT NOT NULL,
    time_admin_id BIGINT NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    data_banimento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_banimento_liga_liga FOREIGN KEY (liga_id) REFERENCES liga(id),
    CONSTRAINT fk_banimento_liga_time_banido FOREIGN KEY (time_banido_id) REFERENCES time(id),
    CONSTRAINT fk_banimento_liga_time_admin FOREIGN KEY (time_admin_id) REFERENCES time(id)
);

CREATE INDEX idx_banimento_liga_liga_ativo ON banimento_liga(liga_id, ativo);
CREATE INDEX idx_banimento_liga_time_ativo ON banimento_liga(liga_id, time_banido_id, ativo);
