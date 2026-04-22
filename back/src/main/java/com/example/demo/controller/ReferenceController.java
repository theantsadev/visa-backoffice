package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.ReferenceService;
import com.example.demo.model.Nationnalite;
import com.example.demo.model.SituationFamiliale;
import com.example.demo.model.TypeDemande;
import com.example.demo.model.TypeVisa;

@RestController
@RequestMapping("/api")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping("/types-visa")
    public List<TypeVisa> listerTypesVisa() {
        return referenceService.listerTypesVisa();
    }

    @GetMapping("/types-demande")
    public List<TypeDemande> listerTypesDemande() {
        return referenceService.listerTypesDemande();
    }

    @GetMapping("/nationalites")
    public List<Nationnalite> listerNationalites() {
        return referenceService.listerNationalites();
    }

    @GetMapping("/statuts-familiaux")
    public List<SituationFamiliale> listerStatutsFamiliaux() {
        return referenceService.listerStatutsFamiliaux();
    }
}
