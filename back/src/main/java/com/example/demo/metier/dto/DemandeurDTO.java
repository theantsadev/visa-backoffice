package com.example.demo.metier.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DemandeurDTO {
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String nomJeuneFille;
    private String adresseMada;
    private String telephone;
    private String email;
    private Integer idNationalite;
    private Integer idStatutFamilial;
}
