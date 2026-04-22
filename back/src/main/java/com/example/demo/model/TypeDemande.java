package com.example.demo.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_demande_effectuee")
@Data
public class TypeDemande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_demande_effectuee")
    private Integer id;

    private String libelle;
}
