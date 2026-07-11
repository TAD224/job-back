-- ============================================================
-- Migration : Processus de candidature enrichi
-- Contexte : le CV devient obligatoire par candidature, ajout de la lettre
-- de motivation en fichier, du téléphone de contact et de la disponibilité.
--
-- ⚠️ Si des candidatures existent déjà sans CV en base (cv_path NULL),
-- l'ALTER TABLE en NOT NULL échouera. Dans ce cas, mets d'abord à jour
-- les lignes concernées avec une valeur par défaut avant de relancer ce script :
--   UPDATE candidatures SET cv_path = 'non-fourni' WHERE cv_path IS NULL;
-- ============================================================

ALTER TABLE candidatures
    ADD COLUMN IF NOT EXISTS lettre_motivation_path VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS telephone_contact VARCHAR(30) NULL,
    ADD COLUMN IF NOT EXISTS date_disponibilite DATE NULL;

-- Rendre le CV obligatoire (à exécuter seulement après avoir vérifié qu'aucune
-- ligne existante n'a cv_path à NULL, cf. avertissement ci-dessus).
ALTER TABLE candidatures
    MODIFY COLUMN cv_path VARCHAR(255) NOT NULL;
