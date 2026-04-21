package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.VisaTransformable;

public interface VisaTransformableRepository extends JpaRepository<VisaTransformable, Integer> {
}
