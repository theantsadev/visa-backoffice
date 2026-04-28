package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "demande_nouveau_titre")
@Data
public class DemandeNouveauTitre {

    @Id
    @Column(name = "id_demande_nouveau_titre")
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_demande_nouveau_titre")
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable")
    private VisaTransformable visaTransformable;

    @ManyToOne
    @JoinColumn(name = "id_passeport", nullable = false)
    private Passport passport;

    @ManyToOne
    @JoinColumn(name = "id_type_visa", nullable = false)
    private TypeVisa typeVisa;
}
