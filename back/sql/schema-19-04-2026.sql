CREATE TABLE
   type_visa (
      id_type_visa SERIAL,
      libelle VARCHAR(50),
      duree INTEGER,
      PRIMARY KEY (id_type_visa)
   );

CREATE TABLE
   statut_familial (
      id_statut_familial INTEGER,
      libelle VARCHAR(50),
      PRIMARY KEY (id_statut_familial)
   );

CREATE TABLE
   type_demande (
      id_type_demande SERIAL,
      libelle VARCHAR(50),
      PRIMARY KEY (id_type_demande)
   );

CREATE TABLE
   nationnalite (
      id_nationnalite SERIAL,
      libelle VARCHAR(50),
      PRIMARY KEY (id_nationnalite)
   );

CREATE TABLE
   statut_demande (
      statut_demande SERIAL,
      libelle VARCHAR(50),
      PRIMARY KEY (statut_demande)
   );

CREATE TABLE
   piece_a_fournir (
      id_piece_a_fournir SERIAL,
      nom VARCHAR(500),
      obligatoire BOOLEAN DEFAULT TRUE,
      id_type_demande INTEGER,
      id_type_visa INTEGER,
      PRIMARY KEY (id_piece_a_fournir),
      FOREIGN KEY (id_type_demande) REFERENCES type_demande (id_type_demande),
      FOREIGN KEY (id_type_visa) REFERENCES type_visa (id_type_visa)
   );

CREATE TABLE
   demandeur (
      id_demandeur SERIAL,
      nom VARCHAR(50) NOT NULL,
      prenom VARCHAR(50),
      date_naissance DATE NOT NULL,
      nom_jeune_fille VARCHAR(50),
      adresse_mada TEXT,
      telephone VARCHAR(50) NOT NULL,
      email VARCHAR(50),
      id_nationnalite INTEGER NOT NULL,
      id_statut_familial INTEGER NOT NULL,
      PRIMARY KEY (id_demandeur),
      FOREIGN KEY (id_nationnalite) REFERENCES nationnalite (id_nationnalite),
      FOREIGN KEY (id_statut_familial) REFERENCES statut_familial (id_statut_familial)
   );

CREATE TABLE
   passport (
      id_passport SERIAL,
      numero VARCHAR(50),
      date_delivrance DATE,
      date_expiration DATE,
      id_demandeur INTEGER NOT NULL,
      PRIMARY KEY (id_passport),
      FOREIGN KEY (id_demandeur) REFERENCES demandeur (id_demandeur)
   );

CREATE TABLE
   visa (
      id_visa SERIAL,
      reference_visa VARCHAR(50),
      date_fin DATE,
      date_debut DATE,
      id_type_visa INTEGER NOT NULL,
      id_passport INTEGER NOT NULL,
      PRIMARY KEY (id_visa),
      FOREIGN KEY (id_type_visa) REFERENCES type_visa (id_type_visa),
      FOREIGN KEY (id_passport) REFERENCES passport (id_passport)
   );

CREATE TABLE
   carte_resident (
      id_carte_resident SERIAL,
      reference_visa VARCHAR(50),
      date_fin DATE,
      date_debut DATE,
      id_type_visa INTEGER NOT NULL,
      id_passport INTEGER NOT NULL,
      PRIMARY KEY (id_carte_resident),
      FOREIGN KEY (id_type_visa) REFERENCES type_visa (id_type_visa),
      FOREIGN KEY (id_passport) REFERENCES passport (id_passport)
   );

CREATE TABLE
   visa_transformable (
      id_visa_transformable SERIAL,
      reference_visa VARCHAR(50),
      date_entree_mada DATE,
      lieu_entree_mada VARCHAR(50),
      date_sortie DATE,
      id_passport INTEGER NOT NULL,
      id_demandeur INTEGER NOT NULL,
      PRIMARY KEY (id_visa_transformable),
      FOREIGN KEY (id_passport) REFERENCES passport (id_passport),
      FOREIGN KEY (id_demandeur) REFERENCES demandeur (id_demandeur)
   );

CREATE TABLE
   demande (
      id_demande SERIAL,
      date_demande TIMESTAMP,
      id_demandeur INTEGER NOT NULL,
      id_type_demande INTEGER NOT NULL,
      PRIMARY KEY (id_demande),
      FOREIGN KEY (id_demandeur) REFERENCES demandeur (id_demandeur),
      FOREIGN KEY (id_type_demande) REFERENCES type_demande (id_type_demande)
   );

CREATE TABLE
   demande_nouveau_titre (
      id_demande_nouveau_titre INTEGER,
      id_visa_transformable INTEGER,
      id_passeport INTEGER NOT NULL,
      id_type_visa INTEGER NOT NULL,
      PRIMARY KEY (id_demande_nouveau_titre),
      FOREIGN KEY (id_demande_nouveau_titre) REFERENCES demande (id_demande),
      FOREIGN KEY (id_visa_transformable) REFERENCES visa_transformable (id_visa_transformable),
      FOREIGN KEY (id_passeport) REFERENCES passport (id_passport),
      FOREIGN KEY (id_type_visa) REFERENCES type_visa (id_type_visa)
   );

CREATE TABLE
   demande_duplicata_carte_resident (
      id_demande_duplicata_carte_resident INTEGER,
      id_demande_nouveau_titre_source INTEGER,
      PRIMARY KEY (id_demande_duplicata_carte_resident),
      FOREIGN KEY (id_demande_duplicata_carte_resident) REFERENCES demande (id_demande),
      FOREIGN KEY (id_demande_nouveau_titre_source) REFERENCES demande_nouveau_titre (id_demande_nouveau_titre)
   );

CREATE TABLE
   demande_transfert_visa (
      id_demande_transfert_visa INTEGER,
      id_passeport INTEGER NOT NULL,
      id_demande_nouveau_titre_source INTEGER,
      PRIMARY KEY (id_demande_transfert_visa),
      FOREIGN KEY (id_demande_transfert_visa) REFERENCES demande (id_demande),
      FOREIGN KEY (id_passeport) REFERENCES passport (id_passport),
      FOREIGN KEY (id_demande_nouveau_titre_source) REFERENCES demande_nouveau_titre (id_demande_nouveau_titre)
   );

CREATE TABLE
   piece_jointe (
      id_piece_jointe SERIAL,
      id_piece_a_fournir INTEGER NOT NULL,
      lien TEXT,
      id_demande INTEGER NOT NULL,
      PRIMARY KEY (id_piece_jointe),
      FOREIGN KEY (id_piece_a_fournir) REFERENCES piece_a_fournir (id_piece_a_fournir),
      FOREIGN KEY (id_demande) REFERENCES demande (id_demande)
   );



CREATE TABLE
   historique_statut_demande (
      id_demande INTEGER,
      statut_demande INTEGER,
      date_heure_historique TIMESTAMP,
      PRIMARY KEY (id_demande, statut_demande),
      FOREIGN KEY (id_demande) REFERENCES demande (id_demande),
      FOREIGN KEY (statut_demande) REFERENCES statut_demande (statut_demande)
   );

-- =========================
-- Donnees de reference
-- =========================
-- Types de visa
INSERT INTO
   type_visa (id_type_visa, libelle)
VALUES
   (1, 'Travailleur'),
   (2, 'Investisseur');

-- Types de demande
INSERT INTO
   type_demande (id_type_demande, libelle)
VALUES
   (1, 'Nouveau titre'),
   (2, 'Duplicata'),
   (3, 'Transfert');

-- Statuts d'une demande
INSERT INTO
   statut_demande (statut_demande, libelle)
VALUES
   (1, 'Dossier cree'),
   (2, 'Scan termine'),
   (3, 'Visa accorde'),
   (4, 'Visa rejete');

-- Situation familiale
INSERT INTO
   statut_familial (id_statut_familial, libelle)
VALUES
   (1, 'Celibataire'),
   (2, 'Marie(e)'),
   (3, 'Divorce(e)'),
   (4, 'Veuf/Veuve');

-- Nationalite (au moins une donnee)
INSERT INTO
   nationnalite (id_nationnalite, libelle)
VALUES
   (1, 'Malagasy');

-- Pieces a fournir
-- Regles:
-- - commune a tous: id_type_visa = NULL et id_type_demande = NULL
-- - specifique visa: id_type_visa renseigne, id_type_demande = NULL
-- - specifique demande: id_type_demande renseigne, id_type_visa = NULL
INSERT INTO
   piece_a_fournir (
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

