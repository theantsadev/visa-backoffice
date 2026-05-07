(function (window) {
    const payloadModels = Object.freeze({
        demandeur: Object.freeze({
            nom: { type: "string", required: true },
            prenom: { type: "string", required: false },
            dateNaissance: { type: "string", required: true },
            nomJeuneFille: { type: "string", required: false },
            adresseMada: { type: "string", required: false },
            telephone: { type: "string", required: true },
            email: { type: "string", required: false },
            idNationalite: { type: "number", required: true },
            idStatutFamilial: { type: "number", required: true }
        }),
        passport: Object.freeze({
            numero: { type: "string", required: true },
            dateDelivrance: { type: "string", required: false },
            dateExpiration: { type: "string", required: false }
        }),
        visaTransformable: Object.freeze({
            referenceVisa: { type: "string", required: true },
            natureVisa: { type: "string", required: false },
            dateEntreeMada: { type: "string", required: true },
            lieuEntreeMada: { type: "string", required: false },
            dateSortie: { type: "string", required: false }
        }),
        pieceJointe: Object.freeze({
            idPieceAFournir: { type: "number", required: true },
            lien: { type: "string", required: false }
        }),
        demande: Object.freeze({
            numero: { type: "string", required: true },
            idDemandeur: { type: "number", required: true },
            idPassport: { type: "number", required: true },
            idVisaTransformable: { type: "number", required: false },
            idTypeVisa: { type: "number", required: true },
            idTypeDemande: { type: "number", required: true },
            piecesJointes: { type: "PieceJointeDTO[]", required: false },
            demandeur: { type: "DemandeurDTO", required: false },
            passport: { type: "PassportDTO", required: false },
            visaTransformable: { type: "VisaTransformableDTO", required: false }
        }),
        demandeDuplicataSansDonnees: Object.freeze({
            demandeNouveauTitre: { type: "DemandeDTO", required: true },
            piecesCible: { type: "PieceJointeDTO[]", required: true }
        }),
        demandeTransfertSansDonnees: Object.freeze({
            demandeNouveauTitre: { type: "DemandeDTO", required: true },
            idPassportNouveau: { type: "number", required: true },
            piecesCible: { type: "PieceJointeDTO[]", required: true }
        }),
        demandeDetail: Object.freeze({
            id: { type: "number", required: true },
            idTypeVisa: { type: "number", required: true },
            idTypeDemande: { type: "number", required: true },
            dateDemande: { type: "string", required: true },
            statut: { type: "string", required: true },
            typeVisa: { type: "string", required: true },
            typeDemande: { type: "string", required: true },
            demandeur: { type: "DemandeurDetailDTO", required: true },
            passport: { type: "PassportDetailDTO", required: true },
            visaTransformable: { type: "VisaTransformableDetailDTO", required: false },
            piecesJointes: { type: "PieceJointeDetailDTO[]", required: false }
        }),
        demandeListeItem: Object.freeze({
            id: { type: "number", required: true },
            nomDemandeur: { type: "string", required: true },
            typeVisa: { type: "string", required: true },
            typeDemande: { type: "string", required: true },
            statut: { type: "string", required: true },
            dateDemande: { type: "string", required: true }
        })
    });

    const formModels = Object.freeze({
        requestSelection: Object.freeze({
            idTypeDemande: { type: "number", required: true, label: "Type de demande" },
            idTypeVisa: { type: "number", required: true, label: "Type de visa" },
            numeroDemande: { type: "string", required: true, label: "Numero demande" }
        }),
        demandeurForm: Object.freeze({
            nom: { type: "string", required: true, label: "Nom" },
            prenom: { type: "string", required: false, label: "Prenom" },
            dateNaissance: { type: "string", required: true, label: "Date de naissance" },
            nomJeuneFille: { type: "string", required: false, label: "Nom de jeune fille" },
            adresseMada: { type: "string", required: false, label: "Adresse a Madagascar" },
            telephone: { type: "string", required: true, label: "Telephone" },
            email: { type: "string", required: false, label: "Email" },
            idNationalite: { type: "number", required: true, label: "Nationalite" },
            idStatutFamilial: { type: "number", required: true, label: "Situation familiale" }
        }),
        passportForm: Object.freeze({
            numero: { type: "string", required: true, label: "Numero de passeport" },
            dateDelivrance: { type: "string", required: false, label: "Date de delivrance" },
            dateExpiration: { type: "string", required: false, label: "Date d'expiration" }
        }),
        passportNouveauForm: Object.freeze({
            numeroNouveau: { type: "string", required: true, label: "Numero nouveau passeport" },
            dateDelivranceNouveau: { type: "string", required: false, label: "Date de delivrance (nouveau)" },
            dateExpirationNouveau: { type: "string", required: false, label: "Date d'expiration (nouveau)" }
        }),
        visaTransformableForm: Object.freeze({
            referenceVisa: { type: "string", required: true, label: "Reference visa" },
            natureVisa: { type: "string", required: false, label: "Nature visa" },
            dateEntreeMada: { type: "string", required: true, label: "Date entree Madagascar" },
            lieuEntreeMada: { type: "string", required: false, label: "Lieu entree Madagascar" },
            dateSortie: { type: "string", required: false, label: "Date sortie" }
        })
    });

    const stepContracts = Object.freeze({
        1: Object.freeze({ model: "requestSelection" }),
        2: Object.freeze({ model: "demandeurForm" }),
        3: Object.freeze({ model: "passportForm", transferModel: "passportNouveauForm" }),
        4: Object.freeze({ model: "visaTransformableForm" }),
        5: Object.freeze({ model: null })
    });

    window.DemandeContracts = Object.freeze({
        stepMax: 5,
        models: payloadModels,
        formModels: formModels,
        stepContracts: stepContracts
    });
})(window);