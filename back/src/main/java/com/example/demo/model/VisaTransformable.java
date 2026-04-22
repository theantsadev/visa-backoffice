package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table (name = "visa_transformable")
@Data
public class VisaTransformable {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id_visa_transformable")
    private Integer id;

    @Column(name = "reference_visa")
    private String referenceVisa;

    @Column(name = "date_entree_mada")
    private LocalDate dateEntreeMada;

    @Column(name = "lieu_entree_mada")
    private String lieuEntreeMada;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @ManyToOne
    @JoinColumn(name = "id_passport")
    private Passport passport;

    @ManyToOne
    @JoinColumn(name = "id_demandeur")
    private Demandeur demandeur;

}
