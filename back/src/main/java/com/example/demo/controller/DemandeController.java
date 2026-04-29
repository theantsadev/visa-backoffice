package com.example.demo.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.metier.DemandeEffectueeService;
import com.example.demo.metier.dto.DemandeDTO;
import com.example.demo.metier.dto.DemandeDuplicataSansDonneesDTO;
import com.example.demo.metier.dto.DemandeSoumiseDTO;
import com.example.demo.metier.dto.DemandeTransfertSansDonneesDTO;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeEffectueeService demandeEffectueeService;

    public DemandeController(DemandeEffectueeService demandeEffectueeService) {
        this.demandeEffectueeService = demandeEffectueeService;
    }

    @PostMapping(value = "/nouveau-titre", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeSoumiseDTO soumettreDemande(@RequestPart("dto") DemandeDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return demandeEffectueeService.soumettreDemande(dto, files);
    }

    @PostMapping(value = "/duplicata/sans-donnees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeSoumiseDTO soumettreDuplicataSansDonnees(@RequestPart("dto") DemandeDuplicataSansDonneesDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }

        return demandeEffectueeService.soumettreDuplicataSansDonnees(
                dto.getDemandeNouveauTitre(),
                dto.getPiecesCible(),
                files);
    }

    @PostMapping(value = "/transfert/sans-donnees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeSoumiseDTO soumettreTransfertSansDonnees(@RequestPart("dto") DemandeTransfertSansDonneesDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }

        return demandeEffectueeService.soumettreTransfertSansDonnees(
                dto.getDemandeNouveauTitre(),
                dto.getIdPassportNouveau(),
                dto.getPiecesCible(),
                files);
    }
}
