package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.DemandeNouveauTitre;

public interface DemandeNouveauTitreRepository extends JpaRepository<DemandeNouveauTitre, Integer> {
}
