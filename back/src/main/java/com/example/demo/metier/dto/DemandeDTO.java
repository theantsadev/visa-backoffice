package com.example.demo.metier.dto;

import java.util.List;

import lombok.Data;

@Data
public class DemandeDTO {
    private String numero;
    private Integer idDemandeur;
    private Integer idPassport;
    private Integer idVisaTransformable;
    private Integer idTypeVisa;
    private Integer idTypeDemande;
    private List<PieceJointeDTO> piecesJointes;
    private DemandeurDTO demandeur;
    private PassportDTO passport;
    private VisaTransformableDTO visaTransformable;

    @Data
    public static class PieceJointeDTO {
        private Integer idPieceAFournir;
        private String lien;
    }
}
