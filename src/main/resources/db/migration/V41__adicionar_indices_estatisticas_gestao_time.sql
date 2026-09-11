CREATE INDEX idx_partida_mandante_placar_data
    ON partida (id_mandante, status_placar, data_hora DESC);

CREATE INDEX idx_partida_visitante_placar_data
    ON partida (id_visitante, status_placar, data_hora DESC);

CREATE INDEX idx_gestao_time_status_partida
    ON gestao_partida (id_time, status, id_partida);

CREATE INDEX idx_evento_participacao_tipo
    ON evento_sumula (id_participacao, tipo);
