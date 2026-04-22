-- =========================
-- TYPES DE VISA
-- =========================
CREATE TABLE type_visa (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

INSERT INTO type_visa (id, libelle) VALUES
(1, 'Travailleur'),
(2, 'Investisseur');


-- =========================
-- TYPES DE DEMANDE
-- =========================
CREATE TABLE type_demande (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

INSERT INTO type_demande (id, libelle) VALUES
(1, 'Nouveau titre'),
(2, 'Duplicata'),
(3, 'Transfert');


-- =========================
-- STATUTS
-- =========================
CREATE TABLE statut_demande (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

INSERT INTO statut_demande (id, libelle) VALUES
(1, 'Dossier créé'),
(2, 'En cours'),
(3, 'Accepté'),
(4, 'Rejeté');


-- =========================
-- SITUATION FAMILIALE
-- =========================
CREATE TABLE situation_familiale (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

INSERT INTO situation_familiale (id, libelle) VALUES
(1, 'Célibataire'),
(2, 'Marié(e)'),
(3, 'Divorcé(e)'),
(4, 'Veuf/Veuve');


-- =========================
-- DOCUMENTS
-- =========================
CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL
);

INSERT INTO document (id, nom) VALUES
(1, '2 photos identité'),
(2, 'Extrait casier judiciaire'),
(3, 'Notice de renseignement'),
(4, 'Photocopie certifiée passeport'),
(5, 'Certificat de résidence'),
(6, 'Autorisation emploi'),
(7, 'Attestation d''emploi (original)'),
(8, 'Statut de la société'),
(9, 'Extrait registre de commerce'),
(10, 'Carte fiscale'),
(11, 'Déclaration perte/vol'),
(12, 'Ancien passeport'),
(13, 'Photocopie visa transformable');


-- =========================
-- TABLE DE LIAISON (LOGIQUE)
-- =========================
CREATE TABLE piece_requise (
    id SERIAL PRIMARY KEY,
    document_id INT REFERENCES document(id),
    type_visa_id INT REFERENCES type_visa(id),
    type_demande_id INT REFERENCES type_demande(id),
    obligatoire BOOLEAN DEFAULT TRUE
);


-- =========================
-- Nationnalite
-- =========================
CREATE TABLE nationnalite(
   id_nationnalite COUNTER,
   libelle VARCHAR(50),
   PRIMARY KEY(id_nationnalite)
);

INSERT INTO nationnalite (id_nationnalite,libelle) VALUES(1,'Gasy');
