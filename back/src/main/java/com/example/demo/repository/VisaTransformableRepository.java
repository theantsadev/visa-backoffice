package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.VisaTransformable;

public interface VisaTransformableRepository extends JpaRepository<VisaTransformable, Integer> {

    List<VisaTransformable> findTop20ByPassportIdPassportAndDemandeurIdDemandeurOrderByIdDesc(Integer idPassport,
            Integer idDemandeur);
}
