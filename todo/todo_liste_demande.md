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