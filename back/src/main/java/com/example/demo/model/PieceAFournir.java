package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "piece_a_fournir")
@Data
public class PieceAFournir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_piece_a_fournir")
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "obligatoire")
    private Boolean obligatoire;

    @ManyToOne
    @JoinColumn(name = "id_type_demande_effectuee")
    private TypeDemande typeDemande;

    @ManyToOne
    @JoinColumn(name = "id_type_visa")
    private TypeVisa typeVisa;
}
