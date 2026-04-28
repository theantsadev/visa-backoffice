package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Demandeur;

public interface DemandeurRepository extends JpaRepository<Demandeur, Integer> {

	List<Demandeur> findTop20ByOrderByIdDemandeurDesc();
}
