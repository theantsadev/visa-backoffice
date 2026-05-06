# Sprint 2 - Sans donnees anterieures

## Objectif
Integrer les 2 autres types de demande dans l'application :
- Duplicata de carte resident
- Transfert de visa (perte de passeport)

## Perimetre fonctionnel
- CREATE/UPDATE demande de duplicata sans donnees anterieures
- CREATE/UPDATE demande de transfert sans donnees anterieures
- CREATE/UPDATE demande nouveau titre (create deja fait dans sprint 1)

## Regles de gestion
- Si tous les champs/pieces obligatoires sont complets + les optionnels restants, alors statut_demande = dossier_cree ; dossier encore modifiable.
- Si tous les types de champs/pieces sont complets, alors statut_demande = scan_termine ; dossier non modifiable.
- Si accepte par admin (bouton accepte/refuse), alors statut_demande = visa_accorde, sinon visa_rejete ; dossier non modifiable.

## Ecrans + metier (parcours)
1) Choix du type de demande au début (à faire dans index.html si possible)
2) Si type = duplicata carte resident, afficher un checkbox "sans donnees anterieures".
3) Si "sans donnees anterieures" est coche :
   - Rediriger vers le formulaire "nouveau titre".
   - Remplir le formulaire "nouveau titre".
   - Ensuite saisir les champs specifiques de la demande ciblee.
     - Exemple transfert : renseigner le nouveau passeport.
4) La demande "nouveau titre" ne cree pas un visa, et est directement en etat "visa_accorde".
5) Lier la demande "nouveau titre" a la demande ciblee (transfert ou duplicata).

## Base de donnees
1) Maj table statut_demande : ajouter dossier_cree, scan_termine, visa_accorde.
2) Ajouter table carte_resident (meme structure que la table visa).
3) Ajouter colonne duree dans type_visa pour calculer date_expiration = date_debut + duree.
4) Mettre a jour le MCD :

- demande {
  id_demande PK,
  date_demande,
  id_demandeur FK(demandeur),
  id_type_demande FK(type_demande)
}

- demande_nouveau_titre {
  id_demande_nouveau_titre FK(demande) PK,
  id_visa_transformable FK(visa_transformable),
  id_passeport FK(passeport),
  id_type_visa FK(type_visa)
}

- demande_duplicata_carte_resident {
  id_demande_duplicata_carte_resident FK(demande) PK,
  id_demande_nouveau_titre_source FK(demande_nouveau_titre)
}

- demande_transfert_visa {
  id_demande_transfert_visa FK(demande) PK,
  id_passeport FK(passeport),
  id_demande_nouveau_titre_source FK(demande_nouveau_titre)
}

## Etapes d'integration (proposees)
1) BDD
   - Appliquer les changements de schema (statut_demande, carte_resident, type_visa.duree).
   - Mettre a jour les entites et mappings.
2) Modele metier
   - Ajouter les entites demande_duplicata_carte_resident et demande_transfert_visa.
   - Gerer la relation avec demande_nouveau_titre.
3) Services
   - Creer/mettre a jour les services de creation et de mise a jour.
   - Appliquer les regles de statut.
4) Controllers/Routes
   - Ajouter les endpoints pour create/update (duplicata, transfert).
   - Ajouter la logique "sans donnees anterieures".
5) UI
   - Ecran choix type de demande + checkbox.
   - Redirection vers formulaire "nouveau titre" puis champs specifiques.
6) Tests
   - Cas sans donnees anterieures.
   - Transfert avec nouveau passeport.
   - Duplicata carte resident.
   - Verifier transitions de statuts.

## Livrables
- Schema BDD mis a jour.
- Entites + repositories + services.
- Controllers + UI.
- Tests associes.
