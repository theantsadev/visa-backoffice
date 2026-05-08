-- Active: 1774545998122@@127.0.0.1@5433@gestion_visa
-- =====================================================
-- Reset complet des donnees (sans recreation du schema)
-- =====================================================
-- Usage (PostgreSQL):
--   psql -U <user> -d <database> -f sql/reset-donnees.sql

BEGIN;

-- Nettoyage des tables (metier + references)
-- RESTART IDENTITY: remet les sequences a zero
-- CASCADE: gere automatiquement les contraintes FK
TRUNCATE TABLE
    historique_statut_demande,
    piece_jointe,
    demande_transfert_visa,
    demande_duplicata_carte_resident,
    demande_nouveau_titre,
    demande,
    visa_transformable,
    carte_resident,
    visa,
    passport,
    demandeur,
    piece_a_fournir,
    statut_demande,
    nationnalite,
    statut_familial,
    type_demande,
    type_visa
RESTART IDENTITY CASCADE;

-- =========================
-- Donnees de reference
-- =========================

-- Types de visa
INSERT INTO type_visa (id_type_visa, libelle)
VALUES
    (1, 'Travailleur'),
    (2, 'Investisseur');

-- Types de demande
INSERT INTO type_demande (id_type_demande, libelle)
VALUES
    (1, 'Nouveau titre'),
    (2, 'Duplicata'),
    (3, 'Transfert');

-- Statuts d'une demande
INSERT INTO statut_demande (statut_demande, libelle)
VALUES
    (1, 'Dossier cree'),
    (2, 'Photo terminee'),
    (3, 'Scan termine'),
    (4, 'Visa accorde'),
    (5, 'Visa rejete');

-- Situation familiale
INSERT INTO statut_familial (id_statut_familial, libelle)
VALUES
    (1, 'Celibataire'),
    (2, 'Marie(e)'),
    (3, 'Divorce(e)'),
    (4, 'Veuf/Veuve');

-- Nationalite (au moins une donnee)
INSERT INTO nationnalite (id_nationnalite, libelle)
VALUES
    (1, 'Malagasy');

-- Pieces a fournir
-- Regles:
-- - commune a tous: id_type_visa = NULL et id_type_demande = NULL
-- - specifique visa: id_type_visa renseigne, id_type_demande = NULL
-- - specifique demande: id_type_demande renseigne, id_type_visa = NULL
INSERT INTO piece_a_fournir (
    id_piece_a_fournir,
    nom,
    obligatoire,
    id_type_demande,
    id_type_visa
)
VALUES
    (1, '02 photos d''identite', TRUE, NULL, NULL),
    (2, 'Notice de renseignement', TRUE, NULL, NULL),
    (
        3,
        'Demande adressee a M. le Ministre de l''Interieur avec e-mail et telephone portable',
        TRUE,
        NULL,
        NULL
    ),
    (
        4,
        'Photocopie certifiee du visa en cours de validite',
        TRUE,
        NULL,
        NULL
    ),
    (
        5,
        'Photocopie certifiee de la premiere page du passeport',
        TRUE,
        NULL,
        NULL
    ),
    (
        6,
        'Photocopie certifiee de la carte resident en cours de validite',
        TRUE,
        NULL,
        NULL
    ),
    (7, 'Certificat de residence a Madagascar', TRUE, NULL, NULL),
    (8, 'Extrait de casier judiciaire de moins de 3 mois', TRUE, NULL, NULL),
    (9, 'Statut de la societe', TRUE, NULL, 2),
    (
        10,
        'Extrait d''inscription au registre de commerce',
        TRUE,
        NULL,
        2
    ),
    (11, 'Carte fiscale', TRUE, NULL, 2),
    (
        12,
        'Autorisation emploi delivree a Madagascar',
        TRUE,
        NULL,
        1
    ),
    (
        13,
        'Attestation d''emploi delivree par l''employeur (original)',
        TRUE,
        NULL,
        1
    ),
    (
        14,
        'Acte de naissance (enfant) ou acte de mariage (livret de famille)',
        TRUE,
        NULL,
        NULL
    ),
    (
        15,
        'Justificatif de ressources pour regroupement familial',
        TRUE,
        NULL,
        NULL
    ),
    (
        16,
        'Autorisation emploi pour le regroupement familial des travailleurs',
        TRUE,
        NULL,
        1
    );

COMMIT;
