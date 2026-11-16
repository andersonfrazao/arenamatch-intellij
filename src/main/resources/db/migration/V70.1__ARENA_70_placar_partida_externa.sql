-- ARENA-70: placar opcional para partidas externas já realizadas.
-- Os dois valores são informados juntos (0 x 0 exige preenchimento explícito) ou ficam vazios (placar pendente).
ALTER TABLE partida_externa
    ADD COLUMN gols_mandante INTEGER,
    ADD COLUMN gols_visitante INTEGER;

ALTER TABLE partida_externa
    ADD CONSTRAINT ck_partida_externa_placar_completo
        CHECK ((gols_mandante IS NULL) = (gols_visitante IS NULL)),
    ADD CONSTRAINT ck_partida_externa_placar_valido
        CHECK (gols_mandante IS NULL OR (gols_mandante BETWEEN 0 AND 99 AND gols_visitante BETWEEN 0 AND 99));
