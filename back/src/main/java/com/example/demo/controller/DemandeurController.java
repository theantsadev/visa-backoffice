package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.DemandeurService;
import com.example.demo.metier.dto.DemandeurDTO;
import com.example.demo.model.Demandeur;

@RestController
@RequestMapping("/api/demandeurs")
public class DemandeurController {

    private final DemandeurService demandeurService;

    public DemandeurController(DemandeurService demandeurService) {
        this.demandeurService = demandeurService;
    }

    @PostMapping
    public Demandeur creerDemandeur(@RequestBody DemandeurDTO dto) {
        return demandeurService.creerDemandeur(dto);
    }
}
