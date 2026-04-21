package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.DemandeEffectueeService;
import com.example.demo.metier.dto.DemandeDTO;
import com.example.demo.metier.dto.DemandeSoumiseDTO;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeEffectueeService demandeEffectueeService;

    public DemandeController(DemandeEffectueeService demandeEffectueeService) {
        this.demandeEffectueeService = demandeEffectueeService;
    }

    @PostMapping("/nouveau-titre")
    public DemandeSoumiseDTO soumettreDemande(@RequestBody DemandeDTO dto) {
        return demandeEffectueeService.soumettreDemande(dto);
    }
}
