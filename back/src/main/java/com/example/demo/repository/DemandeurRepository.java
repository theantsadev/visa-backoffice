package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Demandeur;

public interface DemandeurRepository extends JpaRepository<Demandeur, Long> {
}
