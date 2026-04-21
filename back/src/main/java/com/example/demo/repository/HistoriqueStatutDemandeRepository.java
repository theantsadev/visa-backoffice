package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.HistoriqueStatutDemande;
import com.example.demo.model.HistoriqueStatutDemandeId;

public interface HistoriqueStatutDemandeRepository
        extends JpaRepository<HistoriqueStatutDemande, HistoriqueStatutDemandeId> {
}
