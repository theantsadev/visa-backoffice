package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "historique_statut_demande")
@IdClass(HistoriqueStatutDemandeId.class)
@Data
public class HistoriqueStatutDemande {

    @Id
    @Column(name = "id_demande_effectuee")
    private Integer idDemandeEffectuee;

    @Id
    @Column(name = "statut_demande")
    private Integer statutDemande;

    @Column(name = "date_heure_historique")
    private LocalDateTime dateHeureHistorique;
}
