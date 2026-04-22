package com.example.demo.metier;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Nationnalite;
import com.example.demo.model.SituationFamiliale;
import com.example.demo.model.TypeDemande;
import com.example.demo.model.TypeVisa;
import com.example.demo.repository.NationnaliteRepository;
import com.example.demo.repository.SituationFamilialeRepository;
import com.example.demo.repository.TypeDemandeRepository;
import com.example.demo.repository.TypeVisaRepository;

@Service
public class ReferenceService {

    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final NationnaliteRepository nationnaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;

    public ReferenceService(
            TypeVisaRepository typeVisaRepository,
            TypeDemandeRepository typeDemandeRepository,
            NationnaliteRepository nationnaliteRepository,
            SituationFamilialeRepository situationFamilialeRepository) {
        this.typeVisaRepository = typeVisaRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.nationnaliteRepository = nationnaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
    }

    public List<TypeVisa> listerTypesVisa() {
        return typeVisaRepository.findAll();
    }

    public List<TypeDemande> listerTypesDemande() {
        return typeDemandeRepository.findAll();
    }

    public List<Nationnalite> listerNationalites() {
        return nationnaliteRepository.findAll();
    }

    public List<SituationFamiliale> listerStatutsFamiliaux() {
        return situationFamilialeRepository.findAll();
    }
}
