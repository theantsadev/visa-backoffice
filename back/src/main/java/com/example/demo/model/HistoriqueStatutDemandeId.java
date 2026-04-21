package com.example.demo.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDemandeId implements Serializable {
    private Long idDemandeEffectuee;
    private Long statutDemande;
}
