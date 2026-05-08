package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PhotoSignature;

public interface PhotoSignatureRepository extends JpaRepository<PhotoSignature, Integer> {
    Optional<PhotoSignature> findByIdDemande(Integer idDemande);
}
