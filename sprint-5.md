# Sprint 5 — Photo et Signature : Fonctionnalité et Design

## Objectif

Rendre l'étape 6 (Photo et Signature) du formulaire de demande de visa **entièrement fonctionnelle** avec un **design premium**, et améliorer l'expérience utilisateur sur la capture caméra et la signature manuscrite.

---

## Résumé des changements

### 1. Backend (déjà en place)

Le backend dispose déjà de toute l'infrastructure nécessaire :

| Composant | Fichier | Rôle |
|---|---|---|
| **Modèle** | `PhotoSignature.java` | Entité JPA mappée sur la table `photo_signature` |
| **Repository** | `PhotoSignatureRepository.java` | Accès à la base avec `findByIdDemande()` |
| **Service** | `DemandeEffectueeService.java` | Méthode `enregistrerPhotoSignature()` — validation, stockage fichier, mise à jour statut |
| **Contrôleur** | `DemandeController.java` | Endpoint `POST /api/demandes/{id}/photo-signature` (multipart) |
| **Table SQL** | `schema-19-04-2026.sql` | Table `photo_signature(id_photo_signature, lien_photo, lien_signature, id_demande)` |

**Flux backend :**
1. Réception des fichiers `photo` (JPEG) et `signature` (PNG) via `MultipartFile`
2. Validation du format image (PNG, JPEG uniquement)
3. Sauvegarde dans `uploads/pieces-jointes/{idDemande}/`
4. Enregistrement en base du lien vers chaque fichier
5. Mise à jour du statut vers `Photo terminée` (ID=2)

---

### 2. Frontend — HTML (`demande-nouveau.html`)

#### Améliorations apportées

**Section Photo :**
- Ajout d'un **en-tête avec icône SVG** (caméra) et **badge de statut** (`En attente`, `Camera active`, `Terminée`, `Erreur`)
- Ajout d'un **placeholder visuel** avec icône et texte indicatif quand la caméra n'est pas active
- Le `<video>` est désormais masqué par défaut (`hidden`) — il n'apparaît qu'une fois la caméra activée
- Boutons avec **icônes SVG intégrées** (caméra, capture, reprendre) pour une meilleure lisibilité
- Boutons `Capturer` et `Reprendre` masqués par défaut (`hidden`) — affichés dynamiquement selon l'état

**Section Signature :**
- En-tête avec **icône stylo SVG** et **badge de statut**
- Ajout d'un **hint superposé** (texte + icône) sur le canvas : `"Dessinez votre signature ici"` — disparaît dès que l'utilisateur commence à dessiner
- Bouton `Effacer` avec icône poubelle SVG

```diff
- <div class="media-card">
-     <p class="media-title">Photo</p>
-     <div class="media-frame">
-         <video id="photoVideo" autoplay playsinline></video>

+ <div class="media-card" id="photoMediaCard">
+     <div class="media-header">
+         <div class="media-header-left">
+             <span class="media-icon"><!-- SVG camera --></span>
+             <p class="media-title">Photo d'identite</p>
+         </div>
+         <span class="media-badge" id="photoBadge">En attente</span>
+     </div>
+     <div class="media-frame" id="photoFrame">
+         <div class="media-placeholder" id="photoPlaceholder">
+             <!-- SVG + texte indicatif -->
+         </div>
+         <video id="photoVideo" autoplay playsinline hidden></video>
```

---

### 3. Frontend — CSS (`demande.css`)

#### Design premium ajouté

| Élément | Avant | Après |
|---|---|---|
| **Cards média** | Fond blanc plat, border simple | Gradient subtil (`#fff → #fef9ee`), barre colorée en haut, hover avec ombre |
| **Frames photo/signature** | Bordure en pointillés basique, fond `#fef7ea` | Bordure `2px dashed` avec gradient de fond, états visuels (`is-active`, `is-captured`) |
| **Badges** | Inexistants | Pilules avec styles `En attente` (jaune), `Terminée` (vert), `Erreur` (rouge) |
| **Icônes** | Aucune | Icônes dans pastilles colorées (gradient orange pour photo, gradient vert pour signature) |
| **Boutons** | Texte seul | Classe `btn-icon` avec icône SVG + texte alignés |
| **Canvas signature** | Pas de curseur spécial | Curseur `crosshair`, hint superposé avec transition d'opacité |
| **Statuts textuels** | Classe `hint` basique | Classe `media-status` avec états `.is-error` (rouge) et `.is-success` (vert) |
| **Transitions** | Aucune | Hover sur cards, scale sur icônes, transitions 280ms sur bordures et ombres |

**Nouvelles classes CSS ajoutées :**
- `.media-header`, `.media-header-left` — layout en-tête
- `.media-icon`, `.media-icon-sig` — pastilles icônes
- `.media-badge`, `.media-badge.is-done`, `.media-badge.is-error` — badges de statut
- `.media-placeholder` — placeholder caméra
- `.signature-hint` — texte indicatif sur canvas
- `.media-frame.is-active`, `.media-frame.is-captured` — états visuels frame
- `.signature-frame.is-active` — état signature en cours
- `.btn-icon` — bouton avec icône
- `.media-status`, `.media-status.is-error`, `.media-status.is-success` — feedback textuel
- `.media-card.is-captured`, `.media-card.is-signed` — état de complétion (barre verte en haut)

---

### 4. Frontend — JavaScript (`demande-app.js`)

#### Gestion des états (machine à états simplifiée)

La section photo suit 3 modes : **`idle` → `streaming` → `captured`**

```
┌─────────┐   Activer camera   ┌────────────┐   Capturer   ┌──────────┐
│  IDLE   │ ────────────────── │ STREAMING  │ ────────────── │ CAPTURED │
│         │                    │            │               │          │
│ [Start] │                    │ [Capture]  │               │ [Retake] │
└─────────┘                    └────────────┘               └──────────┘
     ▲                                                           │
     └──────────────── Reprendre ────────────────────────────────┘
```

**Fonction `syncPhotoButtonStates(mode)` :**
- `idle` : seul le bouton "Activer la caméra" est visible, placeholder affiché
- `streaming` : seul le bouton "Capturer" est visible, frame en mode `is-active` (fond sombre)
- `captured` : seul le bouton "Reprendre" est visible, frame en mode `is-captured` (fond vert clair)

#### Nouvelles fonctions ajoutées

| Fonction | Rôle |
|---|---|
| `updatePhotoBadge(text, state)` | Met à jour le badge photo (texte + classe CSS) |
| `updateSignatureBadge(text, state)` | Met à jour le badge signature |
| `syncPhotoButtonStates(mode)` | Affiche/masque les boutons selon le mode (`idle`, `streaming`, `captured`) |

#### Améliorations sur les fonctions existantes

- **`startCamera()`** : Demande maintenant `facingMode: "user"` et résolution `640×480` pour une meilleure qualité. Met à jour le badge et les boutons
- **`showCapturedPhoto()`** : Active la synchronisation des états
- **`resetPhotoCapture()`** : Réinitialise le badge
- **`clearSignatureCanvas()`** : Réinitialise le badge, le hint et les classes du frame/card
- **`startDrawing()`** (signature) : Active la classe `is-active` sur le frame et masque le hint
- **`draw()`** (signature) : Met à jour le badge "Terminée" et la classe `is-signed` sur la card
- **`capturePhotoButton` listener** : Arrête la caméra après capture (libère le flux vidéo)
- **`syncPhotoSignatureUi()`** : Gestion complète des états visuels en mode édition (photo/signature existantes)
- **`setPhotoStatus()` / `setSignatureStatus()`** : Acceptent un paramètre `isError` pour styler le message

---

## Vérification

- [x] La caméra s'active correctement via `getUserMedia`
- [x] La photo est capturée en JPEG et stockée dans un Blob
- [x] La signature est dessinée sur canvas et exportée en PNG
- [x] Les deux fichiers sont envoyés via `POST /api/demandes/{id}/photo-signature`
- [x] Les boutons s'affichent/masquent selon l'état (idle → streaming → captured)
- [x] Les badges reflètent l'état en temps réel
- [x] Le placeholder disparaît quand la caméra est activée
- [x] Le hint de signature disparaît quand l'utilisateur dessine
- [x] En mode édition, les images existantes sont chargées et les contrôles désactivés
- [x] Le flux caméra est libéré après capture et à la fermeture de la page

---

## Fichiers modifiés

| Fichier | Type de modification |
|---|---|
| `templates/demande-nouveau.html` | Refonte HTML étape 6 (icônes SVG, badges, placeholder, hint) |
| `static/css/demande.css` | Ajout ~180 lignes CSS (cards premium, badges, frames, transitions) |
| `static/js/demande-app.js` | Ajout gestion d'états, badges, synchronisation UI (~120 lignes) |
