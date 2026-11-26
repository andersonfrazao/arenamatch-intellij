-- ARENA-70: o registro manual passa a exigir só adversário, modalidade, categoria e data.
-- Quando o time é visitante, o campo e a taxa do mandante podem ficar sem informação.
-- Quando o time é mandante, eles vêm do cadastro do time, que pode não ter nome de campo ou taxa.
-- O CHECK btrim(nome_campo) <> '' continua valendo para valores preenchidos (NULL passa no CHECK).
ALTER TABLE partida_externa
    ALTER COLUMN nome_campo DROP NOT NULL,
    ALTER COLUMN valor_taxa DROP NOT NULL;
