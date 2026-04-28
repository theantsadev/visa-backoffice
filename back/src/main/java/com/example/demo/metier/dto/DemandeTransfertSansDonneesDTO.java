package com.example.demo.metier.dto;

import java.util.List;

import lombok.Data;

@Data
public class DemandeTransfertSansDonneesDTO {
    private DemandeDTO demandeNouveauTitre;
    private Integer idPassportNouveau;
    private List<DemandeDTO.PieceJointeDTO> piecesCible;
}
