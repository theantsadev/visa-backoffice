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
    private Integer idDemandeur;

    private String nom;

    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "nom_jeune_fille")
    private String nomJeuneFille;

    @Column(name = "adresse_mada")
    private String adresseMada;

    private String telephone;

    private String email;

    @ManyToOne
    @JoinColumn(name = "id_nationnalite")
    private Nationnalite nationnalite;

    @ManyToOne
    @JoinColumn(name = "id_statut_familial")
    private SituationFamiliale situationFamiliale;
}
