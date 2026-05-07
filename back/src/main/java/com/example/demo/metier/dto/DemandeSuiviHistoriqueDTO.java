package com.example.demo.metier.dto;

import java.time.LocalDateTime;

public record DemandeSuiviHistoriqueDTO(
        Integer idStatut,
        String statut,
        LocalDateTime dateHeureHistorique) {
}