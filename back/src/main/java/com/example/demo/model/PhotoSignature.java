package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "photo_signature")
@Data
public class PhotoSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_photo_signature")
    private Integer id;

    @Column(name = "lien_photo")
    private String lienPhoto;

    @Column(name = "lien_signature")
    private String lienSignature;

    @Column(name = "id_demande", nullable = false)
    private Integer idDemande;
}
