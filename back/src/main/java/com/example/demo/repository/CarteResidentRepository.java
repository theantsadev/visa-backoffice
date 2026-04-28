package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CarteResident;

public interface CarteResidentRepository extends JpaRepository<CarteResident, Integer> {
}
