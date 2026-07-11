-- ============================================================
-- Migration : Activation de compte par e-mail
-- Contexte : ajout de la colonne 'enabled' sur 'utilisateurs' et
--            de la table 'activation_tokens'.
--
-- Note : spring.jpa.hibernate.ddl-auto=update créera automatiquement
-- la colonne et la table au prochain démarrage de l'application.
-- Ce script est fourni pour une exécution manuelle en production
-- (ou pour comprendre exactement ce qui a changé), et surtout pour
-- garantir que les comptes déjà existants restent utilisables.
-- ============================================================

-- 1. Ajouter la colonne 'enabled' sur la table utilisateurs.
--    IMPORTANT : les comptes déjà existants passent à enabled = TRUE
--    par défaut, pour ne pas bloquer les utilisateurs déjà inscrits
--    avant la mise en place de ce système. Seules les NOUVELLES
--    inscriptions via /api/auth/register passeront par enabled = FALSE.
ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. Table des tokens d'activation.
CREATE TABLE IF NOT EXISTS activation_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    utilisateur_id BIGINT NOT NULL,
    date_creation DATETIME NOT NULL,
    date_expiration DATETIME NOT NULL,
    CONSTRAINT fk_activation_token_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activation_tokens_token ON activation_tokens(token);
