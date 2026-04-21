package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SituationFamiliale;

public interface SituationFamilialeRepository extends JpaRepository<SituationFamiliale, Long> {
}
