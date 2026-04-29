# Sprint 3 - Upload de pieces jointes

## Objectif
Remplacer la logique de cases a cocher par un vrai upload de fichier par piece jointe, avec persistence du chemin dans la colonne `lien`.

## Changements necessaires

### Base de donnees
- Ajouter `lien TEXT` dans `piece_jointe`.
- Conserver la compatibilite avec les donnees existantes: la colonne doit rester nullable.

### Backoffice / Metier
- Faire accepter les endpoints de soumission en `multipart/form-data`.
- Envoyer le JSON metier dans une partie `dto` et les fichiers dans une liste `files`.
- Persister chaque fichier sur le disque et enregistrer son chemin public dans `piece_jointe.lien`.
- Ajouter un resource handler pour servir les fichiers uploads via `/uploads/**`.
- Ajouter une validation serveur pour verifier qu’une piece obligatoire a bien un fichier associe.

### Ecran de saisie
- Remplacer les checkbox de l’etape 5 par un `input type="file"` pour chaque piece.
- Valider cote client qu’une piece obligatoire a bien un fichier avant soumission.
- Envoyer le formulaire final en multipart au lieu de JSON simple.

### Consultation
- Exposer le lien du fichier dans le detail d’une demande pour pouvoir ouvrir la piece jointe.

## Points d’attention
- Les fichiers ne peuvent pas etre restaures via l’autosave du navigateur, donc un rechargement d’ecran impose de re-selectionner les fichiers.
- Les scripts SQL de remise a zero peuvent rester inchanges si `lien` reste nullable.