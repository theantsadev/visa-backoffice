package com.example.demo.metier.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DemandeDetailDTO(
        Integer id,
        LocalDateTime dateDemande,
        String statut,
        String typeVisa,
        String typeDemande,
        DemandeurDetailDTO demandeur,
        PassportDetailDTO passport,
        VisaTransformableDetailDTO visaTransformable,
        List<PieceJointeDetailDTO> piecesJointes) {

    public record DemandeurDetailDTO(
            Integer idDemandeur,
            String nom,
            String prenom,
            LocalDate dateNaissance,
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
            Boolean fournie) {
    }
}
