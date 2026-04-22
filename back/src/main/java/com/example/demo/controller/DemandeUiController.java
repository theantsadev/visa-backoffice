package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemandeUiController {

    @GetMapping("/")
    public String accueil() {
        return "forward:/index.html";
    }

    @GetMapping("/demande/nouveau")
    public String nouveauParcours() {
        return "forward:/demande-nouveau.html";
    }

    @GetMapping("/demande/confirmation")
    public String confirmationParcours() {
        return "forward:/demande-confirmation.html";
    }

    @GetMapping("/demande/liste")
    public String listeDemandes() {
        return "forward:/demande-liste.html";
    }
}
