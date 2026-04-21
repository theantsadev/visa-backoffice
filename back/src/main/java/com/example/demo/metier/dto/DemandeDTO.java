package com.example.demo.metier.dto;

import java.util.List;

import lombok.Data;

@Data
public class DemandeDTO {
    private Long idDemandeur;
    private Long idPassport;
    private Long idVisaTransformable;
    private Long idTypeVisa;
    private Long idTypeDemande;
    private List<PieceJointeDTO> piecesJointes;

    @Data
    public static class PieceJointeDTO {
        private Long idPieceAFournir;
        private Boolean fournie;
    }
}
