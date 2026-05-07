package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping({ "/demande/suivi/{numero}", "/demande-suivi.html" })
    public String suiviDemande(@PathVariable(required = false) String numero, Model model) {
        prepareViewModel(model);
        model.addAttribute("numero", numero);
        return "demande-suivi";
    }

    private void prepareViewModel(Model model) {
        model.addAttribute("cacheBuster", System.currentTimeMillis());
    }
}
