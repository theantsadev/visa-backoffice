package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Passport;

public interface PassportRepository extends JpaRepository<Passport, Integer> {

	List<Passport> findTop20ByDemandeurIdDemandeurOrderByIdPassportDesc(Integer idDemandeur);
}
