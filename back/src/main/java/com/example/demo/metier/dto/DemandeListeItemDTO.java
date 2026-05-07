package com.example.demo.metier.dto;

import java.time.LocalDateTime;

public record DemandeListeItemDTO(
        Integer id,
        String numero,
        String nomDemandeur,
        String typeVisa,
        String typeDemande,
        String statut,
        LocalDateTime dateDemande) {
}
