package com.example.demo.metier.dto;

import java.time.LocalDate;

public class QuickVisaTransformableDTO {

    private Integer id;
    private String label;
    private String referenceVisa;
    private String natureVisa;
    private LocalDate dateEntreeMada;
    private String lieuEntreeMada;
    private LocalDate dateSortie;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getReferenceVisa() {
        return referenceVisa;
    }

    public void setReferenceVisa(String referenceVisa) {
        this.referenceVisa = referenceVisa;
    }

    public String getNatureVisa() {
        return natureVisa;
    }

    public void setNatureVisa(String natureVisa) {
        this.natureVisa = natureVisa;
    }

    public LocalDate getDateEntreeMada() {
        return dateEntreeMada;
    }

    public void setDateEntreeMada(LocalDate dateEntreeMada) {
        this.dateEntreeMada = dateEntreeMada;
    }

    public String getLieuEntreeMada() {
        return lieuEntreeMada;
    }

    public void setLieuEntreeMada(String lieuEntreeMada) {
        this.lieuEntreeMada = lieuEntreeMada;
    }

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(LocalDate dateSortie) {
        this.dateSortie = dateSortie;
    }
}
