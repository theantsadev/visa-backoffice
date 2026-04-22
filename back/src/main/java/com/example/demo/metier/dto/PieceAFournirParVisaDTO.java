package com.example.demo.metier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PieceAFournirParVisaDTO {
    private Integer idPieceAFournir;
    private String nom;
    private Boolean obligatoire;
    private String categorie;
}
