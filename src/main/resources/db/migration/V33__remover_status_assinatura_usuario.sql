UPDATE usuario
SET status_pagamento = CASE
        WHEN status_assinatura = 'ATIVO' THEN 'PAGO'
        WHEN status_assinatura = 'TRIAL' THEN 'TRIAL'
        ELSE 'EXPIRADO'
    END
WHERE status_pagamento IS NULL;

ALTER TABLE usuario DROP COLUMN IF EXISTS status_assinatura;
