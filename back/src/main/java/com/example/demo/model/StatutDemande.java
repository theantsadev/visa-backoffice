package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="statut_demande")
@Data
public class StatutDemande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_statut_demande")
    private long id;

    private String libelle;

}
