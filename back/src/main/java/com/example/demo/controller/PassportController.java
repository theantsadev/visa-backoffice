package com.example.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.PassportService;
import com.example.demo.metier.dto.PassportDTO;
import com.example.demo.model.Passport;

@RestController
@RequestMapping("/api/demandeurs")
public class PassportController {

    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @PostMapping("/{idDemandeur}/passports")
    public Passport creerPassport(@PathVariable Integer idDemandeur, @RequestBody PassportDTO dto) {
        return passportService.creerPassport(dto, idDemandeur);
    }
}
