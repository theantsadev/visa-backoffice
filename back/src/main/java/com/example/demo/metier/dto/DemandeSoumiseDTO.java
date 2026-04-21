package com.example.demo.metier.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DemandeSoumiseDTO {
    private Long id;
    private String statut;
    private LocalDateTime dateDemande;
}
