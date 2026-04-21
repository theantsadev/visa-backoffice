package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.StatutDemande;

public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Long> {
    Optional<StatutDemande> findByLibelleIgnoreCase(String libelle);
}
