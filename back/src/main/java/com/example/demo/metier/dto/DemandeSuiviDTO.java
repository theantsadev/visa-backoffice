package com.example.demo.metier.dto;

import java.util.List;

public record DemandeSuiviDTO(
                DemandeListeItemDTO demande,
                List<DemandeSuiviHistoriqueDTO> historiqueStatuts) {
}