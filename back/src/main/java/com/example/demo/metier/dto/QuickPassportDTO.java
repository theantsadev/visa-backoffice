package com.example.demo.metier.dto;

import java.time.LocalDate;

public class QuickPassportDTO {

    private Integer idPassport;
    private String label;
    private String numero;
    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;

    public Integer getIdPassport() {
        return idPassport;
    }

    public void setIdPassport(Integer idPassport) {
        this.idPassport = idPassport;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getDateDelivrance() {
        return dateDelivrance;
    }

    public void setDateDelivrance(LocalDate dateDelivrance) {
        this.dateDelivrance = dateDelivrance;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}
