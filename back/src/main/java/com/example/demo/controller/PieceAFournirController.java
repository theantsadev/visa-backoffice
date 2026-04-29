package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metier.PieceAFournirService;
import com.example.demo.metier.dto.PieceAFournirParVisaDTO;

@RestController
@RequestMapping("/api")
public class PieceAFournirController {

    private final PieceAFournirService pieceAFournirService;

    public PieceAFournirController(PieceAFournirService pieceAFournirService) {
        this.pieceAFournirService = pieceAFournirService;
    }

    @GetMapping("/pieces-a-fournir")
    public List<PieceAFournirParVisaDTO> getPiecesParTypeVisa(
            @RequestParam("typeVisa") Integer idTypeVisa,
            @RequestParam(value = "typeDemande", required = false) Integer idTypeDemande) {
        return pieceAFournirService.getPiecesParTypeVisa(idTypeVisa, idTypeDemande);
    }
}
