CREATE TABLE type_visa(
   id_type_visa SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_type_visa)
);

CREATE TABLE statut_familial(
   id_statut_familial INTEGER,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_statut_familial)
);

CREATE TABLE type_demande_effectuee(
   id_type_demande_effectuee SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_type_demande_effectuee)
);

CREATE TABLE nationnalite(
   id_nationnalite SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_nationnalite)
);

CREATE TABLE statut_demande(
   statut_demande SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(statut_demande)
);

CREATE TABLE piece_a_fournir(
   id_piece_a_fournir SERIAL,
   nom VARCHAR(50) ,
   obligatoire BOOLEAN DEFAULT TRUE,
   id_type_demande_effectuee INTEGER,
   id_type_visa INTEGER,
   PRIMARY KEY(id_piece_a_fournir),
   FOREIGN KEY(id_type_demande_effectuee) REFERENCES type_demande_effectuee(id_type_demande_effectuee),
   FOREIGN KEY(id_type_visa) REFERENCES type_visa(id_type_visa)
);

CREATE TABLE demandeur(
   id_demandeur SERIAL,
   nom VARCHAR(50)  NOT NULL,
   prenom VARCHAR(50) ,
   date_naissance DATE NOT NULL,
   nom_jeune_fille VARCHAR(50) ,
   adresse_mada TEXT,
   telephone VARCHAR(50)  NOT NULL,
   email VARCHAR(50) ,
   id_nationnalite INTEGER NOT NULL,
   id_statut_familial INTEGER NOT NULL,
   PRIMARY KEY(id_demandeur),
   FOREIGN KEY(id_nationnalite) REFERENCES nationnalite(id_nationnalite),
   FOREIGN KEY(id_statut_familial) REFERENCES statut_familial(id_statut_familial)
);

CREATE TABLE passport(
   id_passport SERIAL,
   numero VARCHAR(50) ,
   date_delivrance DATE,
   date_expiration DATE,
   id_demandeur INTEGER NOT NULL,
   PRIMARY KEY(id_passport),
   FOREIGN KEY(id_demandeur) REFERENCES demandeur(id_demandeur)
);

CREATE TABLE visa(
   id_visa SERIAL,
   reference_visa VARCHAR(50) ,
   date_fin DATE,
   date_debut DATE,
   id_type_visa INTEGER NOT NULL,
   id_passport INTEGER NOT NULL,
   PRIMARY KEY(id_visa),
   FOREIGN KEY(id_type_visa) REFERENCES type_visa(id_type_visa),
   FOREIGN KEY(id_passport) REFERENCES passport(id_passport)
);

CREATE TABLE visa_transformable(
   id_visa_transformable SERIAL,
   reference_visa VARCHAR(50) ,
   date_entree_mada DATE,
   lieu_entree_mada VARCHAR(50) ,
   date_sortie DATE,
   id_passport INTEGER NOT NULL,
   id_demandeur INTEGER NOT NULL,
   PRIMARY KEY(id_visa_transformable),
   FOREIGN KEY(id_passport) REFERENCES passport(id_passport),
   FOREIGN KEY(id_demandeur) REFERENCES demandeur(id_demandeur)
);

CREATE TABLE demande_effectuee(
   id_demande_effectuee SERIAL,
   date_demande TIMESTAMP,
   id_passport INTEGER NOT NULL,
   id_visa_transformable INTEGER,
   id_demandeur INTEGER NOT NULL,
   id_type_demande_effectuee INTEGER NOT NULL,
   id_type_visa INTEGER NOT NULL,
   PRIMARY KEY(id_demande_effectuee),
   FOREIGN KEY(id_passport) REFERENCES passport(id_passport),
   FOREIGN KEY(id_visa_transformable) REFERENCES visa_transformable(id_visa_transformable),
   FOREIGN KEY(id_demandeur) REFERENCES demandeur(id_demandeur),
   FOREIGN KEY(id_type_demande_effectuee) REFERENCES type_demande_effectuee(id_type_demande_effectuee),
   FOREIGN KEY(id_type_visa) REFERENCES type_visa(id_type_visa)
);

CREATE TABLE piece_jointe(
   id_piece_jointe SERIAL,
   fournie BOOLEAN,
   id_piece_a_fournir INTEGER NOT NULL,
   id_demande_effectuee INTEGER NOT NULL,
   PRIMARY KEY(id_piece_jointe),
   FOREIGN KEY(id_piece_a_fournir) REFERENCES piece_a_fournir(id_piece_a_fournir),
   FOREIGN KEY(id_demande_effectuee) REFERENCES demande_effectuee(id_demande_effectuee)
);

CREATE TABLE historique_statut_demande(
   id_demande_effectuee INTEGER,
   statut_demande INTEGER,
   date_heure_historique TIMESTAMP,
   PRIMARY KEY(id_demande_effectuee, statut_demande),
   FOREIGN KEY(id_demande_effectuee) REFERENCES demande_effectuee(id_demande_effectuee),
   FOREIGN KEY(statut_demande) REFERENCES statut_demande(statut_demande)
);

-- =========================
-- Donnees de reference
-- =========================

-- Types de visa
INSERT INTO type_visa (id_type_visa, libelle) VALUES
(1, 'Travailleur'),
(2, 'Investisseur');

-- Types de demande
INSERT INTO type_demande_effectuee (id_type_demande_effectuee, libelle) VALUES
(1, 'Nouveau titre'),
(2, 'Duplicata'),
(3, 'Transfert');

-- Statuts d'une demande
INSERT INTO statut_demande (statut_demande, libelle) VALUES
(1, 'Dossier cree'),
(2, 'En cours'),
(3, 'Accepte'),
(4, 'Rejete');

-- Situation familiale
INSERT INTO statut_familial (id_statut_familial, libelle) VALUES
(1, 'Celibataire'),
(2, 'Marie(e)'),
(3, 'Divorce(e)'),
(4, 'Veuf/Veuve');

-- Nationalite (au moins une donnee)
INSERT INTO nationnalite (id_nationnalite, libelle) VALUES
(1, 'Malagasy');

-- Pieces a fournir
-- Regles:
-- - commune a tous: id_type_visa = NULL et id_type_demande_effectuee = NULL
-- - specifique visa: id_type_visa renseigne, id_type_demande_effectuee = NULL
-- - specifique demande: id_type_demande_effectuee renseigne, id_type_visa = NULL
INSERT INTO piece_a_fournir (
   id_piece_a_fournir,
   nom,
   obligatoire,
   id_type_demande_effectuee,
   id_type_visa
) VALUES
(1,  '2 photos identite', TRUE, NULL, NULL),
(2,  'Extrait casier judiciaire', TRUE, NULL, NULL),
(3,  'Notice de renseignement', TRUE, NULL, NULL),
(4,  'Photocopie certifiee passeport', TRUE, NULL, NULL),
(5,  'Certificat de residence', TRUE, NULL, NULL),
(6,  'Autorisation emploi', TRUE, NULL, 1),
(7,  'Attestation d''emploi (original)', TRUE, NULL, 1),
(8,  'Statut de la societe', TRUE, NULL, 2),
(9,  'Extrait registre de commerce', TRUE, NULL, 2),
(10, 'Carte fiscale', TRUE, NULL, 2),
(11, 'Declaration perte/vol', TRUE, 2, NULL),
(12, 'Ancien passeport', TRUE, 3, NULL),
(13, 'Photocopie visa transformable', TRUE, 1, NULL);
