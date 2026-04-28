package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.DemandeDuplicataCarteResident;

public interface DemandeDuplicataCarteResidentRepository
        extends JpaRepository<DemandeDuplicataCarteResident, Integer> {
}
