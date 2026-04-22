package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Passport;

public interface PassportRepository extends JpaRepository<Passport, Integer> {
}
