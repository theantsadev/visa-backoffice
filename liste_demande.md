# Resume global - Partie liste des demandes

Ce document resume ce qui a ete implemente sur le flux de gestion des demandes, avec un focus sur la liste + detail des demandes et sur l'interface associee.

## 1) Ce qui a ete fait (global)

### Commit 1 - Entites de reference
- Mise en place/alignement des entites de reference pour supporter les listes metier.
- La table `piece_a_fournir` est maintenant mappee avec des liens optionnels vers `TypeVisa` et `TypeDemande` (relation JPA), ce qui prepare les filtres de pieces.

### Commit 2 - Repositories de reference
- Ajout des methodes de repository pour distinguer :
  - pieces specifiques au type de visa,
  - pieces specifiques au type de demande,
  - pieces communes.
- Ces methodes servent de base aux services metier et au chargement des pieces a l'etape UI.

### Commit 3 - API de references
- Ajout d'un service + controller pour exposer les listes de reference via HTTP :
  - GET /api/types-visa
  - GET /api/types-demande
  - GET /api/nationalites
  - GET /api/statuts-familiaux

### Commit 4 - Pieces a fournir par type de visa
- Ajout d'un service + controller dedies au chargement des pieces.
- Endpoint expose :
  - GET /api/pieces-a-fournir?typeVisa={id}
- La reponse contient les pieces communes + specifiques avec indication de categorie.

### Commit 5 - Liste et detail des demandes
- Ajout d'un service + controller pour les besoins backoffice :
  - GET /api/demandes -> liste resumee
  - GET /api/demandes/{id} -> detail complet
- La liste retournee contient :
  - id, nomDemandeur, typeVisa, typeDemande, statut, dateDemande
- Le detail retourne :
  - informations demandeur,
  - passeport,
  - visa transformable,
  - pieces jointes,
  - statut actuel.
- Ajout de methodes repository necessaires :
  - lecture du dernier statut de l'historique,
  - lecture des pieces jointes d'une demande.

## 2) Nouveautes recentes (interface liste des demandes)

Une nouvelle interface ergonomique a ete ajoutee pour consulter les demandes :

- Page UI : GET /demande/liste
- Fonctionnalites :
  - chargement de la liste des demandes,
  - recherche rapide (nom, type, statut, etc.),
  - panneau de detail au clic sur une ligne,
  - affichage des blocs demande/demandeur/passeport/visa/pieces.
- Le design suit la charte visuelle existante (meme famille typographique, palette, composants, responsive).
- Lien ajoute depuis la page d'accueil pour acceder rapidement a la liste.

## 3) Fichiers importants modifies pour la partie liste des demandes

### Backend (API liste/detail)
- back/src/main/java/com/example/demo/controller/DemandeListeController.java
- back/src/main/java/com/example/demo/metier/DemandeListeService.java
- back/src/main/java/com/example/demo/metier/dto/DemandeListeItemDTO.java
- back/src/main/java/com/example/demo/metier/dto/DemandeDetailDTO.java
- back/src/main/java/com/example/demo/repository/PieceJointeRepository.java
- back/src/main/java/com/example/demo/repository/HistoriqueStatutDemandeRepository.java

### Frontend (interface liste/detail)
- back/src/main/resources/static/demande-liste.html
- back/src/main/resources/static/ui/demande-liste.js
- back/src/main/resources/static/ui/demande.css
- back/src/main/resources/static/index.html
- back/src/main/java/com/example/demo/controller/DemandeUiController.java

## 4) Commandes de lancement du projet (Windows / PowerShell)

## 4.1 Prerequis
- Java 17 installe
- Maven installe
- PostgreSQL lance
- Base configuree selon `back/src/main/resources/application.properties`
  - URL : jdbc:postgresql://localhost:5432/gestion_visa
  - User : postgres
  - Password : admin

## 4.2 Charger le schema SQL (si base vide)
Depuis la racine du repo :

```powershell
psql -h localhost -U postgres -d gestion_visa -f "back/sql/schema-19-04-2026.sql"
```

## 4.3 Lancer l'application Spring Boot

```powershell
cd back
mvn spring-boot:run
```

## 4.4 Verifier rapidement les endpoints API (optionnel)

```powershell
curl http://localhost:8080/api/demandes
curl http://localhost:8080/api/demandes/1
```

## 4.5 Ouvrir les pages UI
- Accueil : http://localhost:8080/
- Nouvelle demande : http://localhost:8080/demande/nouveau
- Liste + detail des demandes : http://localhost:8080/demande/liste

## 5) Conseils de verification fonctionnelle

- Creer au moins une demande via /demande/nouveau.
- Aller sur /demande/liste.
- Verifier :
  - la presence de la demande dans la liste,
  - le statut affiche,
  - le detail complet au clic,
  - la section des pieces jointes.
