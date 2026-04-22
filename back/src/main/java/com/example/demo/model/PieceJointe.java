package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "piece_jointe")
@Data
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_piece_jointe")
    private Integer id;

    @Column(name = "fournie")
    private Boolean fournie;

    @Column(name = "id_piece_a_fournir")
    private Integer idPieceAFournir;

    @Column(name = "id_demande_effectuee")
    private Integer idDemandeEffectuee;
}
