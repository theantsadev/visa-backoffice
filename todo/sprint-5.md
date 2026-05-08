# Sprint 5 - Photo, signature et statut "photo terminee"

**TL : Mike | BO : Jordi | Support : Vicky**

---

## Objectif
Ajouter la prise de photo par webcam et la signature par trackpad, avec un nouveau statut intermediaire **"photo terminee"** avant le scan final.

## Flux cible
```
dossier cree (modifiable,obligatoire complet)
    -> photo terminee (photo + signature completes, modifiable)
    -> scan termine (tout type, non modifiable)
    -> visa accorde (valide par admin, non modifiable)
```

## Base fonctionnelle existante a reutiliser
- Parcours de creation et edition de demande dans `back/src/main/java/com/example/demo/metier/DemandeEffectueeService.java`
- Endpoints de demande dans `back/src/main/java/com/example/demo/controller/DemandeController.java`
- Page de saisie principale dans `back/src/main/resources/templates/demande-nouveau.html`
- Logique front du formulaire dans `back/src/main/resources/static/js/demande-app.js`
- Style commun du parcours dans `back/src/main/resources/static/css/demande.css`
- Statut courant et historique deja geres via `HistoriqueStatutDemande`

---

## Mike

### Lot 1 - Cadrer le modele metier et la base

- [x] Ajouter la table `photo_signature` dans le script SQL de reference, avec les colonnes minimum `id`, `lien_photo`, `lien_signature`, et la cle de rattachement vers la demande ou le demandeur selon le choix d'architecture. Reprendre la logique de migration deja presente dans `back/sql/schema-19-04-2026.sql` et `back/sql/alter-06-05-2026.sql`.
- [x] Ajouter le statut **"photo terminee"** dans les donnees de reference des statuts, en conservant l'ordre metier des statuts deja utilises par `DemandeEffectueeService`.
- [x] Verifier et ajuster les constantes de statut dans `back/src/main/java/com/example/demo/metier/DemandeEffectueeService.java` pour introduire la nouvelle etape sans casser les transitions existantes `dossier cree`, `scan termine`, `visa accorde`.
- [x] Definir la regle de non-blocage: une demande au statut `photo terminee` reste modifiable, contrairement a `scan termine` et `visa accorde`.
- [x] Ajouter au besoin le repository ou l'entite manquante pour persister `photo_signature`, en suivant le style des entites et repositories deja en place dans `back/src/main/java/com/example/demo/model` et `back/src/main/java/com/example/demo/repository`.

### Lot 2 - Couvrir les regles de transition

- [x] Ajouter les controles de transition dans `DemandeEffectueeService` pour empecher un passage direct vers `scan termine` si la photo ou la signature n'est pas enregistree.
- [x] Faire en sorte que la validation admin conserve la logique actuelle autour de `DemandeController.accepter()` et `DemandeController.refuser()`, tout en permettant d'acter le passage final a `visa accorde` apres `scan termine`.

---

## Jordi

### Lot 1 - Exposer les operations de photo et signature

- [ ] Ajouter dans `DemandeController` les endpoints necessaires pour enregistrer la photo et la signature, en consommant du `multipart/form-data` comme les autres routes du module `demande`.
- [ ] Ajouter une methode metier equivalente a `uploadPieceJointe` dans `DemandeEffectueeService` pour stocker la photo ou meme signature sur disque, en reutilisant le dossier de televersement configure par `app.upload-dir`.
- [ ] Ajouter une methode `enregistrerPhotoSignature(id_demande, lien_photo, lien_signature)` dans `DemandeEffectueeService`, avec persistance de la ligne `photo_signature` et retour d'un objet exploitable par le front.
- [ ] Centraliser les validations metier: fichier manquant, format invalide, demande inexistante, demande non modifiable, photo ou signature deja renseignee.
- [ ] Si la demande de type edition existe deja dans le code, brancher la nouvelle etape sur le flux de mise a jour sans dupliquer la logique de creation.

### Lot 2 - Controle du verrouillage dossier

- [ ] Faire en sorte que le statut `photo terminee` laisse encore la demande editable, mais que `scan termine` verrouille definitivement le dossier.
- [ ] Verifier dans le service que la modification de demande ne peut pas contourner le verrouillage une fois le dossier passe en `scan termine`.
- [ ] Aligner les messages d'erreur avec le reste du backend, qui repose sur `IllegalArgumentException` pour les regles metier.

### Lot 3 - Tests d'API

- [ ] Ajouter des tests d'integration pour `POST /api/demandes/...` ou les nouvelles routes photo/signature, sur le modele des endpoints deja exposes dans `back/src/main/java/com/example/demo/controller/DemandeController.java`.
- [ ] Ajouter un test de non-regression pour s'assurer qu'une demande en `photo terminee` reste modifiable alors qu'une demande en `scan termine` ne l'est plus.

---

## Vicky

### Lot 1 - Ecran photo et signature

- [ ] Etendre le parcours front dans `back/src/main/resources/templates/demande-nouveau.html` avec une 6e etape dediee a la **prise de photo et signature**.
- [ ] Mettre a jour `back/src/main/resources/static/js/demande-app.js` pour gerer cette nouvelle etape, en gardant la logique existante des 5 etapes et du stepper.
- [ ] Ajouter les fonctions front `getPhoto()` et `getSignature()` pour recuperer respectivement le flux webcam et le tracage au trackpad.
- [ ] Adapter la configuration du stepper et de la progression pour passer de 5 a 6 etapes sans casser l'etat actuel des panneaux.
- [ ] Ajouter la zone UI pour la webcam, le canevas de signature et les actions de re-essai / effacement.

### Lot 2 - Integration front / back

- [ ] Brancher l'envoi des donnees photo/signature sur la nouvelle methode backend, avec un payload coherent avec le reste du formulaire multipart.
- [ ] Afficher le statut `photo terminee` apres validation de la photo et de la signature, avant le scan final.
- [ ] Prevoir un retour visuel clair si la photo ou la signature a echoue, sans perdre les donnees deja saisies.
- [ ] Mettre a jour la partie confirmation si necessaire, pour refleter les nouvelles etapes du dossier.

### Lot 3 - UX et robustesse

- [ ] Mettre a jour `back/src/main/resources/static/css/demande.css` pour les nouveaux blocs webcam / signature / preview.
- [ ] Verifier le comportement sur ecran large et mobile, surtout pour le dessin de signature et la capture webcam.
- [ ] Ajouter les cas limites cote front: absence de camera, refus d'autorisation, canevas vide, fichier photo trop volumineux.

---

## Definition de fini
- [ ] La demande peut passer par les 4 etats cibles: `dossier cree` -> `photo terminee` -> `scan termine` -> `visa accorde`.
- [ ] Une demande en `photo terminee` reste modifiable.
- [ ] Une demande en `scan termine` devient non modifiable.
- [ ] La photo et la signature sont persistées en base.
- [ ] La nouvelle etape est utilisable depuis l'interface de creation/modification.
- [ ] Les tests critiques passent sur le module demande.
