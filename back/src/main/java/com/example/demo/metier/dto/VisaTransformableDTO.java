package com.example.demo.metier.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class VisaTransformableDTO {
    private String referenceVisa;
    private String natureVisa;
    private LocalDate dateEntreeMada;
    private String lieuEntreeMada;
    private LocalDate dateSortie;
}
