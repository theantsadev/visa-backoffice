package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "passport")
@Data
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_passport")
    private long idPassport;

    private String numero;

    private LocalDate dateDelivrence;

    private LocalDate dateExpiration;

    @ManyToOne
    @JoinColumn(name="id_demandeur")
    private Demandeur demandeur;

}
