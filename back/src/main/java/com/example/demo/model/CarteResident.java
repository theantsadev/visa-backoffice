package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "carte_resident")
@Data
public class CarteResident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carte_resident")
    private Integer id;

    @Column(name = "reference_visa")
    private String referenceVisa;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @ManyToOne
    @JoinColumn(name = "id_type_visa", nullable = false)
    private TypeVisa typeVisa;

    @ManyToOne
    @JoinColumn(name = "id_passport", nullable = false)
    private Passport passport;
}
