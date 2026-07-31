ALTER TABLE publicacao_liga ALTER COLUMN liga_id DROP NOT NULL;

CREATE INDEX idx_publicacao_liga_status_criacao ON publicacao_liga(status, data_criacao);
