# Sprint 4 - Suivi de demande et QR

## Objectif
Ajouter un vrai suivi de demande depuis le backoffice et fournir un QR code qui renvoie vers l'URL de suivi.

## Perimetre fonctionnel
- Consulter une demande via son `id_demande`.
- Consulter les demandes rattachees a un `id_passport`.
- Afficher la demande de reference, les demandes associees et l'historique des statuts.
- Generer un QR code sur la page de confirmation pour ouvrir la page de suivi.
- Ajouter une page UI dediee au suivi.

## Endpoints ajoutes
- `GET /api/demandes/suivi/{numeroId}`
- `GET /demande/suivi/{numeroId}`

## Comportement
- Si `numeroId` est un id de demande, le suivi met en avant cette demande puis affiche les autres demandes du meme demandeur.
- Si `numeroId` est un id de passeport, le suivi retrouve la demande de reference associee au passeport, puis affiche les demandes du meme demandeur.
- La page de confirmation affiche un QR code pointant vers la page de suivi.

## Livrables
- Controleur de suivi expose en HTTP.
- DTO de suivi.
- Page de suivi backoffice.
- QR code de confirmation.
- Styles et scripts front dedies.
