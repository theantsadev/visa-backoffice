package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "demande_duplicata_carte_resident")
@Data
public class DemandeDuplicataCarteResident {

    @Id
    @Column(name = "id_demande_duplicata_carte_resident")
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_demande_duplicata_carte_resident")
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_demande_nouveau_titre_source")
    private DemandeNouveauTitre demandeNouveauTitreSource;
}
