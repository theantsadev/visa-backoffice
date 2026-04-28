package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.ReferenceService;
import com.example.demo.metier.dto.QuickDemandeurDTO;
import com.example.demo.metier.dto.QuickPassportDTO;
import com.example.demo.metier.dto.QuickVisaTransformableDTO;
import com.example.demo.model.Demandeur;
import com.example.demo.model.Passport;
import com.example.demo.model.Nationnalite;
import com.example.demo.model.SituationFamiliale;
import com.example.demo.model.TypeDemande;
import com.example.demo.model.TypeVisa;
import com.example.demo.model.VisaTransformable;
import com.example.demo.repository.DemandeurRepository;
import com.example.demo.repository.PassportRepository;
import com.example.demo.repository.VisaTransformableRepository;

@RestController
@RequestMapping("/api")
public class ReferenceController {

    private final ReferenceService referenceService;
    private final DemandeurRepository demandeurRepository;
    private final PassportRepository passportRepository;
    private final VisaTransformableRepository visaTransformableRepository;

    public ReferenceController(
            ReferenceService referenceService,
            DemandeurRepository demandeurRepository,
            PassportRepository passportRepository,
            VisaTransformableRepository visaTransformableRepository) {
        this.referenceService = referenceService;
        this.demandeurRepository = demandeurRepository;
        this.passportRepository = passportRepository;
        this.visaTransformableRepository = visaTransformableRepository;
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

    @GetMapping("/demandeurs-rapides")
    public List<QuickDemandeurDTO> listerDemandeursRapides() {
        return demandeurRepository.findTop20ByOrderByIdDemandeurDesc().stream()
                .map(this::toQuickDemandeur)
                .collect(Collectors.toList());
    }

    @GetMapping("/passports-rapides")
    public List<QuickPassportDTO> listerPassportsRapides(@RequestParam("idDemandeur") Integer idDemandeur) {
        if (idDemandeur == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }

        return passportRepository.findTop20ByDemandeurIdDemandeurOrderByIdPassportDesc(idDemandeur).stream()
                .map(this::toQuickPassport)
                .collect(Collectors.toList());
    }

    @GetMapping("/visas-transformables-rapides")
    public List<QuickVisaTransformableDTO> listerVisasRapides(
            @RequestParam("idDemandeur") Integer idDemandeur,
            @RequestParam("idPassport") Integer idPassport) {
        if (idDemandeur == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }

        if (idPassport == null) {
            throw new IllegalArgumentException("L'id du passeport est obligatoire.");
        }

        return visaTransformableRepository
                .findTop20ByPassportIdPassportAndDemandeurIdDemandeurOrderByIdDesc(idPassport, idDemandeur).stream()
                .map(this::toQuickVisa)
                .collect(Collectors.toList());
    }

    private QuickDemandeurDTO toQuickDemandeur(Demandeur entity) {
        QuickDemandeurDTO dto = new QuickDemandeurDTO();
        dto.setIdDemandeur(entity.getIdDemandeur());
        dto.setLabel(buildDemandeurLabel(entity));
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setDateNaissance(entity.getDateNaissance());
        dto.setNomJeuneFille(entity.getNomJeuneFille());
        dto.setAdresseMada(entity.getAdresseMada());
        dto.setTelephone(entity.getTelephone());
        dto.setEmail(entity.getEmail());
        dto.setIdNationalite(entity.getNationnalite() == null ? null : entity.getNationnalite().getId());
        dto.setIdStatutFamilial(entity.getSituationFamiliale() == null ? null : entity.getSituationFamiliale().getId());
        return dto;
    }

    private QuickPassportDTO toQuickPassport(Passport entity) {
        QuickPassportDTO dto = new QuickPassportDTO();
        dto.setIdPassport(entity.getIdPassport());
        dto.setLabel(buildPassportLabel(entity));
        dto.setNumero(entity.getNumero());
        dto.setDateDelivrance(entity.getDateDelivrance());
        dto.setDateExpiration(entity.getDateExpiration());
        return dto;
    }

    private QuickVisaTransformableDTO toQuickVisa(VisaTransformable entity) {
        QuickVisaTransformableDTO dto = new QuickVisaTransformableDTO();
        dto.setId(entity.getId());
        dto.setLabel(buildVisaLabel(entity));
        dto.setReferenceVisa(entity.getReferenceVisa());
        dto.setDateEntreeMada(entity.getDateEntreeMada());
        dto.setLieuEntreeMada(entity.getLieuEntreeMada());
        dto.setDateSortie(entity.getDateSortie());
        return dto;
    }

    private String buildDemandeurLabel(Demandeur entity) {
        String nom = entity.getNom() == null ? "" : entity.getNom().trim();
        String prenom = entity.getPrenom() == null ? "" : entity.getPrenom().trim();
        String dateNaissance = entity.getDateNaissance() == null ? "?" : entity.getDateNaissance().toString();
        String principal = (nom + " " + prenom).trim();
        if (principal.isEmpty()) {
            principal = "Demandeur";
        }
        return principal + " (" + dateNaissance + ")";
    }

    private String buildPassportLabel(Passport entity) {
        String numero = entity.getNumero() == null ? "Sans numero" : entity.getNumero().trim();
        String expiration = entity.getDateExpiration() == null ? "sans expiration"
                : entity.getDateExpiration().toString();
        return numero + " (exp: " + expiration + ")";
    }

    private String buildVisaLabel(VisaTransformable entity) {
        String ref = entity.getReferenceVisa() == null ? "Sans reference" : entity.getReferenceVisa().trim();
        String entree = entity.getDateEntreeMada() == null ? "sans date" : entity.getDateEntreeMada().toString();
        return ref + " (entree: " + entree + ")";
    }
}
