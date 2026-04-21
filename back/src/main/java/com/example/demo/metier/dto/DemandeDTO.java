package com.example.demo.metier.dto;

import java.util.List;

import lombok.Data;

@Data
public class DemandeDTO {
    private Integer idDemandeur;
    private Integer idPassport;
    private Integer idVisaTransformable;
    private Integer idTypeVisa;
    private Integer idTypeDemande;
    private List<PieceJointeDTO> piecesJointes;

    @Data
    public static class PieceJointeDTO {
        private Integer idPieceAFournir;
        private Boolean fournie;
    }
}
