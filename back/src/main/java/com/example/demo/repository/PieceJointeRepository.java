package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PieceJointe;

public interface PieceJointeRepository extends JpaRepository<PieceJointe, Integer> {
	List<PieceJointe> findByIdDemandeEffectuee(Integer idDemandeEffectuee);
}
