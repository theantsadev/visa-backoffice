# Sprint 1

**TL : Mike | BO : Jordi | Support : Vicky**

---

## Comment ça marche (le flux en entier)

L'utilisateur remplit une demande de visa en 5 étapes dans l'ordre.
Il ne peut pas passer à l'étape suivante si un champ obligatoire est vide.
À la fin, tout est enregistré d'un coup avec le statut **"dossier créé"**.

```
Étape 1 — Qui es-tu ?         → on enregistre le demandeur
Étape 2 — Ton passeport ?     → on enregistre le passeport
Étape 3 — Ton visa actuel ?   → on enregistre le visa transformable
Étape 4 — Ce que tu veux ?    → on choisit le type de demande + type de visa
Étape 5 — Tes documents ?     → on coche les pièces à fournir
         ↓
Soumission → statut = "dossier créé"
```

---

## Ce qu'on met dans la base au départ (données fixes)

Ces données sont insérées une seule fois au démarrage du projet.

**Types de visa**
| id | ce que c'est |
|----|-------------|
| 1 | Travailleur |
| 2 | Investisseur |

**Types de demande**
| id | ce que c'est |
|----|-------------|
| 1 | Nouveau titre |
| 2 | Duplicata |
| 3 | Transfert |

**Statuts d'une demande**
| id | ce que c'est |
|----|-------------|
| 1 | Dossier créé |
| 2 | En cours |
| 3 | Accepté |
| 4 | Rejeté |

**Situation familiale**
| id | ce que c'est |
|----|-------------|
| 1 | Célibataire |
| 2 | Marié(e) |
| 3 | Divorcé(e) |
| 4 | Veuf/Veuve |

**Pièces à fournir**
| id | document | obligatoire ? | pour quel type visa | pour quel type demande |
|----|----------|---------------|----------------------|------------------------|
| 1 | 2 photos identité | oui | tout le monde | tout le monde |
| 2 | Extrait casier judiciaire | oui | tout le monde | tout le monde |
| 3 | Notice de renseignement | oui | tout le monde | tout le monde |
| 4 | Photocopie certifiée passeport | oui | tout le monde | tout le monde |
| 5 | Certificat de résidence | oui | tout le monde | tout le monde |
| 6 | Autorisation emploi | oui | Travailleur | — |
| 7 | Attestation d'emploi (original) | oui | Travailleur | — |
| 8 | Statut de la société | oui | Investisseur | — |
| 9 | Extrait registre de commerce | oui | Investisseur | — |
| 10 | Carte fiscale | oui | Investisseur | — |
| 11 | Déclaration perte/vol | oui | — | Duplicata |
| 12 | Ancien passeport | oui | — | Transfert |
| 13 | Photocopie visa transformable | oui | — | Nouveau titre |

> Les pièces 1 à 5 sont **communes** à tout le monde.
> Les pièces 6 à 13 sont **spécifiques** selon ce que le demandeur choisit.

---

## 👷 Vicky

### Fonctionnalité 1 — Saisie d'une demande (nouveau titre)
`branche : sprint-1-saisie-demande`

---

#### Commit 1 — Créer les tables en base
`bdd : creation des tables PostgreSQL`

Créer le script SQL avec toutes les tables en syntaxe PostgreSQL
(remplacer COUNTER → SERIAL et LOGICAL → BOOLEAN).

Tables à créer : `demandeur`, `passport`, `visa_transformable`,
`demande_effectuee`, `piece_jointe`

---

#### Commit 2 — Remplir les tables de référence
`bdd : insert des donnees de reference`

Insérer les données fixes listées plus haut dans les tables :
`type_visa`, `type_demande_effectuee`, `statut_demande`,
`statut_familial`, `piece_a_fournir`

---

#### Commit 3 — Créer les objets Java qui représentent les tables
`metier : entites demandeur passport visa_transformable`

Créer les classes Java annotées `@Entity` qui correspondent
à chaque table. Spring les utilise pour lire/écrire en base.

```
Demandeur         → table demandeur
Passport          → table passport
VisaTransformable → table visa_transformable
DemandeEffectuee  → table demande_effectuee
PieceJointe       → table piece_jointe
```

> Chaque entité doit référencer les entités de Vicky
> (TypeVisa, TypeDemande, StatutDemande, Nationalite, StatutFamilial)

---

#### Commit 4 — Créer les objets de transfert (ce qu'on reçoit du front)
`metier : dto demandeur passport visa_transformable demande`

Les DTO sont les objets qu'on reçoit dans les requêtes HTTP.
Ils ne touchent pas la base directement.

```
DemandeurDTO      → { nom, prenom, dateNaissance, nomJeuneFille,
                      adresseMada, telephone, email,
                      idNationalite, idStatutFamilial }

PassportDTO       → { numero, dateDelivrance, dateExpiration }

VisaTransformableDTO → { referenceVisa, natureVisa, dateEntreeMada,
                         lieuEntreeMada, dateSortie }

DemandeDTO        → { idDemandeur, idPassport, idVisaTransformable,
                      idTypeVisa, idTypeDemande,
                      piecesJointes: [{ idPieceAFournir, fournie }] }
```

---

#### Commit 5 — Enregistrer le demandeur
`metier : DemandeurService enregistrement et validation`

Classe `DemandeurService` — fonction `creerDemandeur(DemandeurDTO dto)`

Ce que fait cette fonction :
- vérifie que nom, dateNaissance, telephone ne sont pas vides
- vérifie que la nationalité choisie existe bien en base
- vérifie que la situation familiale choisie existe bien en base
- enregistre le demandeur dans la table `demandeur`
- retourne le demandeur créé avec son id

---

#### Commit 6 — Enregistrer le passeport
`metier : PassportService enregistrement et validation`

Classe `PassportService` — fonction `creerPassport(PassportDTO dto, Integer idDemandeur)`

Ce que fait cette fonction :
- vérifie que le demandeur existe bien en base
- vérifie que le numéro de passeport n'est pas vide
- vérifie que la date d'expiration est après la date de délivrance
- enregistre le passeport dans la table `passport`
- retourne le passeport créé avec son id

---

#### Commit 7 — Enregistrer le visa transformable
`metier : VisaTransformableService enregistrement et validation`

Classe `VisaTransformableService` — fonction `creerVisaTransformable(VisaTransformableDTO dto, Integer idPassport)`

Ce que fait cette fonction :
- vérifie que le passeport existe bien en base
- vérifie que la référence visa et la date d'entrée ne sont pas vides
- enregistre le visa dans la table `visa_transformable`
- retourne le visa créé avec son id

---

#### Commit 8 — Soumettre la demande complète
`metier : DemandeEffectueeService soumission finale`

Classe `DemandeEffectueeService` — deux fonctions :

**`soumettreDemande(DemandeDTO dto)`**

Ce que fait cette fonction :
- vérifie que le demandeur, le passeport et le visa existent en base
- vérifie que le type de visa et le type de demande existent en base
- appelle `validerPiecesObligatoires()` avant d'aller plus loin
- enregistre la demande dans `demande_effectuee` avec statut "dossier créé"
- enregistre une ligne dans `historique_statut_demande`
- appelle `savePiecesJointes()` pour enregistrer les pièces cochées
- retourne la demande créée avec son id et son statut

**`validerPiecesObligatoires(List<PieceJointeDTO> pieces, Integer idTypeVisa)`**

Ce que fait cette fonction :
- récupère toutes les pièces obligatoires pour ce type de visa
  (communes + spécifiques)
- vérifie que chacune est bien cochée (fournie = true) dans la liste reçue
- bloque et envoie une erreur si une pièce obligatoire manque

**`savePiecesJointes(List<PieceJointeDTO> pieces, Integer idDemande)`**

Ce que fait cette fonction :
- enregistre chaque pièce cochée dans la table `piece_jointe`
  avec l'id de la demande

---

#### Commit 9 — Exposer les endpoints HTTP
`metier : controllers saisie demande`

Créer les classes controller qui reçoivent les appels HTTP du front.

```
DemandeurController
  POST /api/demandeurs
  → reçoit DemandeurDTO
  → appelle DemandeurService.creerDemandeur()
  → retourne le demandeur créé

PassportController
  POST /api/demandeurs/{idDemandeur}/passports
  → reçoit PassportDTO
  → appelle PassportService.creerPassport()
  → retourne le passeport créé

VisaTransformableController
  POST /api/passports/{idPassport}/visas-transformables
  → reçoit VisaTransformableDTO
  → appelle VisaTransformableService.creerVisaTransformable()
  → retourne le visa créé

DemandeController
  POST /api/demandes/nouveau-titre
  → reçoit DemandeDTO
  → appelle DemandeEffectueeService.soumettreDemande()
  → retourne { id, statut: "dossier créé", dateDemande }
```

---

## 👷 Vicky — Support Jordi + Liste des demandes
`branche : sprint-1-liste-demandes`

> Vicky livre les commits 1 et 2 en **J2 au plus tard**
> car Jordi en a besoin pour avancer sur ses entités.

---

#### Commit 1 — Créer les objets Java pour les données de référence
`metier : entites de reference (typeVisa, typeDemande, statut...)`

Créer les classes `@Entity` pour toutes les tables de référence.
Ces classes sont partagées et utilisées par Jordi aussi.

```
TypeVisa         → table type_visa
TypeDemande      → table type_demande_effectuee
StatutDemande    → table statut_demande
Nationalite      → table nationalite
StatutFamilial   → table statut_familial
PieceAFournir    → table piece_a_fournir
                   (avec lien optionnel vers TypeVisa et TypeDemande)
HistoriqueStatutDemande → table historique_statut_demande
```

---

#### Commit 2 — Créer les accès en base pour les données de référence
`metier : repositories de reference`

Créer les `Repository` pour chaque entité de référence.
Ajouter trois fonctions spéciales dans `PieceAFournirRepository` :

```
findByTypeVisa_IdTypeVisa(Integer idTypeVisa)
→ récupère les pièces spécifiques à un type de visa

findByTypeDemande_IdTypeDemande(Integer idTypeDemande)
→ récupère les pièces spécifiques à un type de demande

findByTypeVisaIsNullAndTypeDemandeIsNull()
→ récupère les pièces communes à tout le monde
```

---

#### Commit 3 — Donner accès aux listes de référence via HTTP
`metier : ReferenceService et ReferenceController`

Classe `ReferenceService` — fonctions qui lisent les tables de référence :

```
listerTypesVisa()       → retourne tous les types de visa
listerTypesDemande()    → retourne tous les types de demande
listerNationalites()    → retourne toutes les nationalités
listerStatutsFamiliaux() → retourne toutes les situations familiales
```

Classe `ReferenceController` — endpoints qui exposent ces listes :

```
GET /api/types-visa          → liste des types de visa
GET /api/types-demande       → liste des types de demande
GET /api/nationalites        → liste des nationalités
GET /api/statuts-familiaux   → liste des situations familiales
```

---

#### Commit 4 — Charger les pièces à fournir selon le type de visa
`metier : PieceAFournirService et PieceAFournirController`

> ⚠️ Ce commit est la dépendance bloquante pour Jordi.
> Il en a besoin pour valider les pièces obligatoires à la soumission.

Classe `PieceAFournirService` — fonction `getPiecesParTypeVisa(Integer idTypeVisa)` :

Ce que fait cette fonction :
- récupère les pièces communes (pas liées à un type précis)
- récupère les pièces spécifiques au type de visa choisi
- fusionne les deux listes
- indique pour chaque pièce si elle est commune ou spécifique
- retourne la liste complète

Classe `PieceAFournirController` — endpoint :

```
GET /api/pieces-a-fournir?typeVisa={id}
→ retourne la liste des pièces à cocher pour ce type de visa
  (communes + spécifiques, avec indication pour chaque pièce)
```

---

#### Commit 5 — Afficher la liste de toutes les demandes
`metier : DemandeListeService et DemandeListeController`

Classe `DemandeListeService` — deux fonctions :

**`listerToutesLesDemandes()`**

Ce que fait cette fonction :
- récupère toutes les demandes en base
- pour chaque demande, récupère aussi le nom du demandeur,
  le type de visa, le type de demande et le statut actuel
- retourne un résumé de chaque demande

**`getDetailDemande(Integer idDemande)`**

Ce que fait cette fonction :
- récupère la demande par son id
- récupère toutes les infos liées : demandeur, passeport,
  visa transformable, pièces jointes
- retourne tout en un seul objet

Classe `DemandeListeController` — endpoints :

```
GET /api/demandes
→ retourne la liste résumée de toutes les demandes
  { id, nomDemandeur, typeVisa, typeDemande, statut, dateDemande }

GET /api/demandes/{id}
→ retourne le détail complet d'une demande
```

---

## ⚠️ Ce que Mike doit surveiller

| Quand | Quoi |
|-------|------|
| Jour 1 | Valider le script SQL de Jordi avant que tout le monde commence |
| Jour 2 | Vicky livre ses entités → Jordi peut avancer sur `DemandeEffectuee` |
| Jour 4 | Vicky livre `GET /api/pieces-a-fournir` → Jordi peut finir la soumission |
| Jour 6 | Revue de code + test du flux complet sur Postman |
