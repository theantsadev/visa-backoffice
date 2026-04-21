package com.example.demo.metier.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PassportDTO {
    private String numero;
    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;
}
