package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Demande;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {

    List<Demande> findByDemandeur_IdDemandeurOrderByDateDemandeAsc(Integer idDemandeur);

    Optional<Demande> findByNumero(String numero);

    boolean existsByNumero(String numero);
}
