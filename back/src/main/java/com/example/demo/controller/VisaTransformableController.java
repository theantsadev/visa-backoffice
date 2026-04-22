package com.example.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.VisaTransformableService;
import com.example.demo.metier.dto.VisaTransformableDTO;
import com.example.demo.model.VisaTransformable;

@RestController
@RequestMapping("/api/passports")
public class VisaTransformableController {

    private final VisaTransformableService visaTransformableService;

    public VisaTransformableController(VisaTransformableService visaTransformableService) {
        this.visaTransformableService = visaTransformableService;
    }

    @PostMapping("/{idPassport}/visas-transformables")
    public VisaTransformable creerVisaTransformable(@PathVariable Integer idPassport, @RequestBody VisaTransformableDTO dto) {
        return visaTransformableService.creerVisaTransformable(dto, idPassport);
    }
}
