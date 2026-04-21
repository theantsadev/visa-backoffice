package com.example.demo.metier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.metier.dto.PassportDTO;
import com.example.demo.model.Demandeur;
import com.example.demo.model.Passport;
import com.example.demo.repository.DemandeurRepository;
import com.example.demo.repository.PassportRepository;

@Service
public class PassportService {

    private final PassportRepository passportRepository;
    private final DemandeurRepository demandeurRepository;

    public PassportService(PassportRepository passportRepository, DemandeurRepository demandeurRepository) {
        this.passportRepository = passportRepository;
        this.demandeurRepository = demandeurRepository;
    }

    @Transactional
    public Passport creerPassport(PassportDTO dto, Integer idDemandeur) {
        if (dto == null) {
            throw new IllegalArgumentException("Le passeport est obligatoire.");
        }

        if (idDemandeur == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }

        Demandeur demandeur = demandeurRepository.findById(idDemandeur)
                .orElseThrow(() -> new IllegalArgumentException("Demandeur introuvable pour id=" + idDemandeur));

        if (isBlank(dto.getNumero())) {
            throw new IllegalArgumentException("Le numero de passeport est obligatoire.");
        }

        if (dto.getDateDelivrance() != null && dto.getDateExpiration() != null
                && !dto.getDateExpiration().isAfter(dto.getDateDelivrance())) {
            throw new IllegalArgumentException("La date d'expiration doit etre apres la date de delivrance.");
        }

        Passport passport = new Passport();
        passport.setNumero(dto.getNumero().trim());
        passport.setDateDelivrance(dto.getDateDelivrance());
        passport.setDateExpiration(dto.getDateExpiration());
        passport.setDemandeur(demandeur);

        return passportRepository.save(passport);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
