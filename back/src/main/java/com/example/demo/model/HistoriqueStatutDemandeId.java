package com.example.demo.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDemandeId implements Serializable {
    private Integer idDemandeEffectuee;
    private Integer statutDemande;
}
