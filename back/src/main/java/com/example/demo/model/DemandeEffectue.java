package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "demande_effectuee")
@Data
public class DemandeEffectue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande_effectuee")
    private Integer id;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @ManyToOne
    @JoinColumn(name = "id_passport", nullable = false)
    private Passport passport;

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable")
    private VisaTransformable visaTransformable;

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;

    @ManyToOne
    @JoinColumn(name = "id_type_demande_effectuee", nullable = false)
    private TypeDemande typeDemande;

    @ManyToOne
    @JoinColumn(name = "id_type_visa", nullable = false)
    private TypeVisa typeVisa;

}
