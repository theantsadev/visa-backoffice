package com.example.demo.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping(value = "/nouveau-titre/{idDemande}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeSoumiseDTO modifierDemande(@PathVariable Integer idDemande,
            @RequestPart("dto") DemandeDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return demandeEffectueeService.mettreAJourDemandeNouveauTitre(idDemande, dto, files);
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

    @PutMapping(value = "/duplicata/sans-donnees/{idDemande}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeSoumiseDTO modifierDuplicataSansDonnees(@PathVariable Integer idDemande,
            @RequestPart("dto") DemandeDuplicataSansDonneesDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }

        return demandeEffectueeService.mettreAJourDuplicataSansDonnees(
                idDemande,
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

    @PutMapping(value = "/transfert/sans-donnees/{idDemande}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DemandeSoumiseDTO modifierTransfertSansDonnees(@PathVariable Integer idDemande,
            @RequestPart("dto") DemandeTransfertSansDonneesDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }

        return demandeEffectueeService.mettreAJourTransfertSansDonnees(
                idDemande,
                dto.getDemandeNouveauTitre(),
                dto.getIdPassportNouveau(),
                dto.getPiecesCible(),
                files);
    }

    @PostMapping("/{idDemande}/accepte")
    public DemandeSoumiseDTO accepter(@PathVariable Integer idDemande) {
        return demandeEffectueeService.validerParAdmin(idDemande, "Visa accorde");
    }

    @PostMapping("/{idDemande}/refuse")
    public DemandeSoumiseDTO refuser(@PathVariable Integer idDemande) {
        return demandeEffectueeService.validerParAdmin(idDemande, "Visa rejete");
    }
}
