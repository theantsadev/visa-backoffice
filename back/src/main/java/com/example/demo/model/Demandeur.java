package com.example.demo.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "demandeur")
@Data
public class Demandeur {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id_demandeur")
    private long idDemandeur;

    private String nom;

    private String prenom;

    private LocalDate dateNaissance;

    private String nomJeuneFille;

    private String adresse_mada;

    private String telephone;

    private String email;

    @ManyToOne
    @JoinColumn(name = "id_nationnalite")
    private Nationnalite nationnalite;

    @ManyToOne
    @JoinColumn(name = "id_situation_familiale")
    private SituationFamiliale situation_familiale;
}
