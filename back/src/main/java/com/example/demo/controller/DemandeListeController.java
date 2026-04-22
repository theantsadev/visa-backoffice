package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.DemandeListeService;
import com.example.demo.metier.dto.DemandeDetailDTO;
import com.example.demo.metier.dto.DemandeListeItemDTO;

@RestController
@RequestMapping("/api/demandes")
public class DemandeListeController {

    private final DemandeListeService demandeListeService;

    public DemandeListeController(DemandeListeService demandeListeService) {
        this.demandeListeService = demandeListeService;
    }

    @GetMapping
    public List<DemandeListeItemDTO> listerToutesLesDemandes() {
        return demandeListeService.listerToutesLesDemandes();
    }

    @GetMapping("/{id}")
    public DemandeDetailDTO getDetailDemande(@PathVariable("id") Integer idDemande) {
        return demandeListeService.getDetailDemande(idDemande);
    }
}
