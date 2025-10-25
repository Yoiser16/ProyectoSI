-- =====================================================
-- GDPR Consent Migration - Agregar campos de consentimiento
-- =====================================================
-- Este script agrega los campos necesarios para el sistema de consentimiento GDPR
-- Nota: Si usas spring.jpa.hibernate.ddl-auto=update, estas columnas se crearán automáticamente

USE proyectosi;

-- Agregar campo de aceptación de política de privacidad (obligatorio)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS privacy_policy_accepted TINYINT(1) DEFAULT 0;

-- Agregar campo de aceptación de correos de marketing (opcional)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS marketing_emails_accepted TINYINT(1) DEFAULT 0;

-- Agregar campo de fecha de consentimiento
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS consent_date DATETIME(6) NULL;

-- Actualizar usuarios existentes (marcar como no consentidos para forzar aceptación en próximo login)
UPDATE users 
SET privacy_policy_accepted = 0,
    marketing_emails_accepted = 0,
    consent_date = NULL
WHERE privacy_policy_accepted IS NULL;

-- Verificar la estructura
DESCRIBE users;

-- Ver cantidad de usuarios que necesitan dar consentimiento
SELECT COUNT(*) as usuarios_sin_consentimiento 
FROM users 
WHERE privacy_policy_accepted = 0 OR privacy_policy_accepted IS NULL;

COMMIT;
