package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Demande;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
}
