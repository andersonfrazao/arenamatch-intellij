ALTER TABLE usuario ADD COLUMN codigo_acesso_admin VARCHAR(60);
ALTER TABLE usuario ADD COLUMN validade_codigo_acesso_admin TIMESTAMP;
ALTER TABLE usuario ADD COLUMN token_desafio_admin VARCHAR(36);
ALTER TABLE usuario ADD COLUMN tentativas_codigo_admin INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN data_envio_codigo_admin TIMESTAMP;

CREATE UNIQUE INDEX uk_usuario_token_desafio_admin
    ON usuario (token_desafio_admin)
    WHERE token_desafio_admin IS NOT NULL;
