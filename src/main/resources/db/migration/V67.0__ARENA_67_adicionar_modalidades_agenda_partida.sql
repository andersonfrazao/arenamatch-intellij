CREATE TABLE time_modalidade (
    id_time BIGINT NOT NULL REFERENCES time(id),
    modalidade VARCHAR(40) NOT NULL,
    PRIMARY KEY (id_time, modalidade),
    CHECK (modalidade IN ('FUTEBOL_CAMPO', 'FUTSAL', 'FUT_7', 'FUTEBOL_SOCIETY'))
);

INSERT INTO time_modalidade (id_time, modalidade)
SELECT id, 'FUTEBOL_CAMPO' FROM time;

ALTER TABLE agenda ADD COLUMN modalidade VARCHAR(40);
UPDATE agenda SET modalidade = 'FUTEBOL_CAMPO';
ALTER TABLE agenda ALTER COLUMN modalidade SET NOT NULL;
ALTER TABLE agenda ADD CONSTRAINT fk_agenda_modalidade
    FOREIGN KEY (id_time, modalidade) REFERENCES time_modalidade(id_time, modalidade);

ALTER TABLE partida ADD COLUMN modalidade VARCHAR(40);
UPDATE partida SET modalidade = 'FUTEBOL_CAMPO';
ALTER TABLE partida ALTER COLUMN modalidade SET NOT NULL;
ALTER TABLE partida ADD CONSTRAINT fk_partida_mandante_modalidade
    FOREIGN KEY (id_mandante, modalidade) REFERENCES time_modalidade(id_time, modalidade);
ALTER TABLE partida ADD CONSTRAINT fk_partida_visitante_modalidade
    FOREIGN KEY (id_visitante, modalidade) REFERENCES time_modalidade(id_time, modalidade);

CREATE INDEX idx_partida_modalidade_placar ON partida(modalidade, status_placar);
CREATE INDEX idx_agenda_modalidade_dia ON agenda(modalidade, dia_semana);

CREATE FUNCTION impedir_alteracao_modalidade_partida() RETURNS trigger AS $$
BEGIN
    IF NEW.modalidade IS DISTINCT FROM OLD.modalidade THEN
        RAISE EXCEPTION 'A modalidade da partida nao pode ser alterada.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER modalidade_partida_imutavel
BEFORE UPDATE OF modalidade ON partida
FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_modalidade_partida();
