package com.example.demo.metier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.metier.dto.VisaTransformableDTO;
import com.example.demo.model.Passport;
import com.example.demo.model.VisaTransformable;
import com.example.demo.repository.PassportRepository;
import com.example.demo.repository.VisaTransformableRepository;

@Service
public class VisaTransformableService {

    private final VisaTransformableRepository visaTransformableRepository;
    private final PassportRepository passportRepository;

    public VisaTransformableService(
            VisaTransformableRepository visaTransformableRepository,
            PassportRepository passportRepository) {
        this.visaTransformableRepository = visaTransformableRepository;
        this.passportRepository = passportRepository;
    }

    @Transactional
    public VisaTransformable creerVisaTransformable(VisaTransformableDTO dto, Integer idPassport) {
        if (dto == null) {
            throw new IllegalArgumentException("Le visa transformable est obligatoire.");
        }

        if (idPassport == null) {
            throw new IllegalArgumentException("L'id du passeport est obligatoire.");
        }

        Passport passport = passportRepository.findById(idPassport)
                .orElseThrow(() -> new IllegalArgumentException("Passeport introuvable pour id=" + idPassport));

        if (isBlank(dto.getReferenceVisa())) {
            throw new IllegalArgumentException("La reference visa est obligatoire.");
        }

        if (dto.getDateEntreeMada() == null) {
            throw new IllegalArgumentException("La date d'entree a Madagascar est obligatoire.");
        }

        VisaTransformable visa = new VisaTransformable();
        visa.setReferenceVisa(dto.getReferenceVisa().trim());
        visa.setDateEntreeMada(dto.getDateEntreeMada());
        visa.setLieuEntreeMada(dto.getLieuEntreeMada());
        visa.setDateSortie(dto.getDateSortie());
        visa.setPassport(passport);
        visa.setDemandeur(passport.getDemandeur());

        return visaTransformableRepository.save(visa);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
