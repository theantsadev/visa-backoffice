package com.example.demo.metier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.metier.dto.DemandeurDTO;
import com.example.demo.model.Demandeur;
import com.example.demo.model.Nationnalite;
import com.example.demo.model.SituationFamiliale;
import com.example.demo.repository.DemandeurRepository;
import com.example.demo.repository.NationnaliteRepository;
import com.example.demo.repository.SituationFamilialeRepository;

@Service
public class DemandeurService {

    private final DemandeurRepository demandeurRepository;
    private final NationnaliteRepository nationnaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;

    public DemandeurService(
            DemandeurRepository demandeurRepository,
            NationnaliteRepository nationnaliteRepository,
            SituationFamilialeRepository situationFamilialeRepository) {
        this.demandeurRepository = demandeurRepository;
        this.nationnaliteRepository = nationnaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
    }

    @Transactional
    public Demandeur creerDemandeur(DemandeurDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Le demandeur est obligatoire.");
        }

        if (isBlank(dto.getNom())) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }

        if (dto.getDateNaissance() == null) {
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
        }

        if (isBlank(dto.getTelephone())) {
            throw new IllegalArgumentException("Le telephone est obligatoire.");
        }

        if (dto.getIdNationalite() == null) {
            throw new IllegalArgumentException("La nationalite est obligatoire.");
        }

        if (dto.getIdStatutFamilial() == null) {
            throw new IllegalArgumentException("La situation familiale est obligatoire.");
        }

        Nationnalite nationnalite = nationnaliteRepository.findById(dto.getIdNationalite())
                .orElseThrow(() -> new IllegalArgumentException("Nationalite introuvable pour id=" + dto.getIdNationalite()));

        SituationFamiliale situationFamiliale = situationFamilialeRepository.findById(dto.getIdStatutFamilial())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Situation familiale introuvable pour id=" + dto.getIdStatutFamilial()));

        Demandeur demandeur = new Demandeur();
        demandeur.setNom(dto.getNom().trim());
        demandeur.setPrenom(dto.getPrenom());
        demandeur.setDateNaissance(dto.getDateNaissance());
        demandeur.setNomJeuneFille(dto.getNomJeuneFille());
        demandeur.setAdresse_mada(dto.getAdresseMada());
        demandeur.setTelephone(dto.getTelephone().trim());
        demandeur.setEmail(dto.getEmail());
        demandeur.setNationnalite(nationnalite);
        demandeur.setSituation_familiale(situationFamiliale);

        return demandeurRepository.save(demandeur);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
