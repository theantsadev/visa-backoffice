package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemandeUiController {

    @GetMapping({ "/", "/index.html" })
    public String accueil(Model model) {
        prepareViewModel(model);
        return "index";
    }

    @GetMapping({ "/demande/nouveau", "/demande-nouveau.html" })
    public String nouveauParcours(Model model) {
        prepareViewModel(model);
        return "demande-nouveau";
    }

    @GetMapping({ "/demande/confirmation", "/demande-confirmation.html" })
    public String confirmationParcours(Model model) {
        prepareViewModel(model);
        return "demande-confirmation";
    }

    @GetMapping({ "/demande/liste", "/demande-liste.html" })
    public String listeDemandes(Model model) {
        prepareViewModel(model);
        return "demande-liste";
    }

    private void prepareViewModel(Model model) {
        model.addAttribute("cacheBuster", System.currentTimeMillis());
    }
}
