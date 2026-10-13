CREATE SEQUENCE seq_campeonato START WITH 1 INCREMENT BY 1;

CREATE TABLE campeonato (
    id BIGINT DEFAULT nextval('seq_campeonato') PRIMARY KEY,
    id_organizador BIGINT NOT NULL REFERENCES usuario(id),
    versao BIGINT NOT NULL DEFAULT 0,
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(4000),
    categoria VARCHAR(80) NOT NULL,
    idade_minima INTEGER CHECK (idade_minima IS NULL OR idade_minima BETWEEN 0 AND 120),
    regulamento VARCHAR(10000),
    data_inicio DATE,
    data_fim DATE,
    dias_sugeridos VARCHAR(200),
    horario_inicio_sugerido TIME,
    taxa_inscricao NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (taxa_inscricao >= 0),
    premiacoes VARCHAR(4000),
    formato VARCHAR(30) NOT NULL CHECK (formato IN ('LIGA','GRUPOS_MATA_MATA','MATA_MATA')),
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO'
        CHECK (status IN ('RASCUNHO','CONVITES','PRONTO_PARA_GERAR','EM_ANDAMENTO','FINALIZADO','CANCELADO')),
    ida_volta_classificatoria BOOLEAN NOT NULL DEFAULT FALSE,
    ida_volta_eliminatoria BOOLEAN NOT NULL DEFAULT FALSE,
    quantidade_times INTEGER CHECK (quantidade_times IS NULL OR quantidade_times >= 2),
    quantidade_grupos INTEGER CHECK (quantidade_grupos IS NULL OR quantidade_grupos >= 2),
    times_por_grupo INTEGER CHECK (times_por_grupo IS NULL OR times_por_grupo >= 2),
    classificados_por_grupo INTEGER CHECK (classificados_por_grupo IS NULL OR classificados_por_grupo >= 1),
    pontos_vitoria INTEGER NOT NULL CHECK (pontos_vitoria >= 0),
    pontos_empate INTEGER NOT NULL CHECK (pontos_empate >= 0),
    pontos_derrota INTEGER NOT NULL CHECK (pontos_derrota >= 0),
    tabela_publicada BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (data_inicio IS NULL OR data_fim IS NULL OR data_fim >= data_inicio),
    CHECK (classificados_por_grupo IS NULL OR times_por_grupo IS NULL OR classificados_por_grupo <= times_por_grupo)
);

CREATE INDEX idx_campeonato_organizador_status ON campeonato(id_organizador, status);

CREATE TABLE campeonato_desempate (
    id_campeonato BIGINT NOT NULL REFERENCES campeonato(id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    criterio VARCHAR(30) NOT NULL CHECK (criterio IN ('VITORIAS','SALDO_GOLS','GOLS_PRO')),
    PRIMARY KEY (id_campeonato, ordem),
    UNIQUE (id_campeonato, criterio)
);
