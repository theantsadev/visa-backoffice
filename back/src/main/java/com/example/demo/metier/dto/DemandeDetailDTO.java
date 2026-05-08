package com.example.demo.metier.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DemandeDetailDTO(
        Integer id,
        String numero,
        Integer idTypeVisa,
        Integer idTypeDemande,
        LocalDateTime dateDemande,
        String statut,
        String typeVisa,
        String typeDemande,
        DemandeurDetailDTO demandeur,
        PassportDetailDTO passport,
        VisaTransformableDetailDTO visaTransformable,
        List<PieceJointeDetailDTO> piecesJointes,
        PhotoSignatureDetailDTO photoSignature) {

    public record DemandeurDetailDTO(
            Integer idDemandeur,
            Integer idNationalite,
            Integer idStatutFamilial,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            String nomJeuneFille,
            String adresseMada,
            String email,
            String telephone) {
    }

    public record PassportDetailDTO(
            Integer idPassport,
            String numero,
            LocalDate dateDelivrance,
            LocalDate dateExpiration) {
    }

    public record VisaTransformableDetailDTO(
            Integer idVisaTransformable,
            String referenceVisa,
            LocalDate dateEntreeMada,
            String lieuEntreeMada,
            LocalDate dateSortie) {
    }

    public record PieceJointeDetailDTO(
            Integer idPieceJointe,
            Integer idPieceAFournir,
            String nomPiece,
            String lien) {
    }

    public record PhotoSignatureDetailDTO(
            String lienPhoto,
            String lienSignature) {
    }
}
