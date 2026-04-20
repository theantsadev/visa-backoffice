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
    private Long id;

    private String referenceVisa;

    @Column(name = "date_entre_mada")
    private LocalDate dateEntreMada;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @ManyToOne
    @JoinColumn(name = "id_passport")
    private Passport passport;

}
