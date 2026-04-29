package com.example.demo.metier.dto;

import java.util.List;

import lombok.Data;

@Data
public class DemandeDuplicataSansDonneesDTO {
    private DemandeDTO demandeNouveauTitre;
    private List<DemandeDTO.PieceJointeDTO> piecesCible;
}
