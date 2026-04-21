package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.TypeDemande;

public interface TypeDemandeRepository extends JpaRepository<TypeDemande, Long> {
}
