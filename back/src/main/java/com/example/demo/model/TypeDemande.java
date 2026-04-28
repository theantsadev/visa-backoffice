package com.example.demo.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_demande")
@Data
public class TypeDemande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_demande")
    private Integer id;

    private String libelle;
}
