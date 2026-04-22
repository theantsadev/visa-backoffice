package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.HistoriqueStatutDemande;
import com.example.demo.model.HistoriqueStatutDemandeId;

public interface HistoriqueStatutDemandeRepository
        extends JpaRepository<HistoriqueStatutDemande, HistoriqueStatutDemandeId> {
        Optional<HistoriqueStatutDemande> findTopByIdDemandeEffectueeOrderByDateHeureHistoriqueDesc(Integer idDemandeEffectuee);
}
