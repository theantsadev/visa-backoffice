package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "demande")
@Data
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Integer id;

    @Column(name = "numero", unique = true, nullable = false)
    private String numero;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;

    @ManyToOne
    @JoinColumn(name = "id_type_demande", nullable = false)
    private TypeDemande typeDemande;

    @OneToOne(mappedBy = "demande")
    private DemandeNouveauTitre demandeNouveauTitre;

    @OneToOne(mappedBy = "demande")
    private DemandeDuplicataCarteResident demandeDuplicataCarteResident;

    @OneToOne(mappedBy = "demande")
    private DemandeTransfertVisa demandeTransfertVisa;
}
