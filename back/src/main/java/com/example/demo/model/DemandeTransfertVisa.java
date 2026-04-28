package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "demande_transfert_visa")
@Data
public class DemandeTransfertVisa {

    @Id
    @Column(name = "id_demande_transfert_visa")
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_demande_transfert_visa")
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_passeport", nullable = false)
    private Passport passport;

    @ManyToOne
    @JoinColumn(name = "id_demande_nouveau_titre_source")
    private DemandeNouveauTitre demandeNouveauTitreSource;
}
