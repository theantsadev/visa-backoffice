(function () {
    const form = document.getElementById("demandeForm");
    if (!form) {
        return;
    }

    const steps = Array.from(document.querySelectorAll(".step"));
    const panels = Array.from(document.querySelectorAll(".step-panel"));
    const feedback = document.getElementById("feedback");
    const piecesContainer = document.getElementById("piecesContainer");
    const btnPrev = document.getElementById("btnPrev");
    const btnNext = document.getElementById("btnNext");
    const btnSubmit = document.getElementById("btnSubmit");

    let currentStep = 1;
    const state = {
        idDemandeur: null,
        idPassport: null,
        idVisaTransformable: null,
        idTypeVisa: null,
        idTypeDemande: null,
        piecesSelection: []
    };

    const pieceCatalog = [
        { id: 1, label: "2 photos identite", scope: "commun" },
        { id: 2, label: "Extrait casier judiciaire", scope: "commun" },
        { id: 3, label: "Notice de renseignement", scope: "commun" },
        { id: 4, label: "Photocopie certifiee passeport", scope: "commun" },
        { id: 5, label: "Certificat de residence", scope: "commun" },
        { id: 6, label: "Autorisation emploi", visa: 1, scope: "visa" },
        { id: 7, label: "Attestation d'emploi (original)", visa: 1, scope: "visa" },
        { id: 8, label: "Statut de la societe", visa: 2, scope: "visa" },
        { id: 9, label: "Extrait registre de commerce", visa: 2, scope: "visa" },
        { id: 10, label: "Carte fiscale", visa: 2, scope: "visa" },
        { id: 11, label: "Declaration perte/vol", demande: 2, scope: "demande" },
        { id: 12, label: "Ancien passeport", demande: 3, scope: "demande" },
        { id: 13, label: "Photocopie visa transformable", demande: 1, scope: "demande" }
    ];

    function setMessage(message, ok) {
        feedback.textContent = message || "";
        feedback.classList.toggle("ok", Boolean(ok));
    }

    function syncUi() {
        steps.forEach((stepEl) => {
            const step = Number(stepEl.dataset.step);
            stepEl.classList.toggle("is-active", step === currentStep);
            stepEl.classList.toggle("is-done", step < currentStep);
        });

        panels.forEach((panel) => {
            panel.classList.toggle("is-active", Number(panel.dataset.stepPanel) === currentStep);
        });

        btnPrev.style.display = currentStep === 1 ? "none" : "inline-block";
        btnNext.style.display = currentStep === 5 ? "none" : "inline-block";
        btnSubmit.style.display = currentStep === 5 ? "inline-block" : "none";
    }

    function required(selector, label) {
        const el = form.querySelector(selector);
        if (!el || !String(el.value || "").trim()) {
            return label + " est obligatoire.";
        }
        return null;
    }

    function validateStep(step) {
        if (step === 1) {
            return required("[name='nom']", "Nom")
                || required("[name='dateNaissance']", "Date de naissance")
                || required("[name='telephone']", "Telephone")
                || required("[name='idNationalite']", "Nationalite")
                || required("[name='idStatutFamilial']", "Situation familiale");
        }
        if (step === 2) {
            return required("[name='numero']", "Numero de passeport");
        }
        if (step === 3) {
            return required("[name='referenceVisa']", "Reference visa")
                || required("[name='dateEntreeMada']", "Date entree Madagascar");
        }
        if (step === 4) {
            return required("[name='idTypeVisa']", "Type de visa")
                || required("[name='idTypeDemande']", "Type de demande");
        }
        if (step === 5) {
            const missing = state.piecesSelection.filter((p) => !p.fournie);
            if (missing.length > 0) {
                return "Toutes les pieces obligatoires doivent etre cochees.";
            }
        }
        return null;
    }

    function getValue(name) {
        const el = form.querySelector("[name='" + name + "']");
        return el ? String(el.value || "").trim() : "";
    }

    function toNumber(name) {
        const value = getValue(name);
        return value ? Number(value) : null;
    }

    function buildPieces() {
        const idTypeVisa = toNumber("idTypeVisa");
        const idTypeDemande = toNumber("idTypeDemande");
        const rows = pieceCatalog.filter((piece) => {
            if (piece.scope === "commun") {
                return true;
            }
            if (piece.scope === "visa") {
                return piece.visa === idTypeVisa;
            }
            return piece.demande === idTypeDemande;
        });

        state.piecesSelection = rows.map((piece) => ({
            idPieceAFournir: piece.id,
            fournie: false
        }));

        piecesContainer.innerHTML = "";
        rows.forEach((piece) => {
            const wrapper = document.createElement("label");
            wrapper.className = "piece-row";

            const left = document.createElement("div");
            left.textContent = piece.label;

            const tag = document.createElement("span");
            tag.className = "piece-tag";
            tag.textContent = piece.scope === "commun" ? "Commune" : (piece.scope === "visa" ? "Specifique visa" : "Specifique demande");
            left.appendChild(tag);

            const check = document.createElement("input");
            check.type = "checkbox";
            check.addEventListener("change", () => {
                const item = state.piecesSelection.find((p) => p.idPieceAFournir === piece.id);
                if (item) {
                    item.fournie = check.checked;
                }
            });

            wrapper.appendChild(left);
            wrapper.appendChild(check);
            piecesContainer.appendChild(wrapper);
        });
    }

    async function postJson(url, body) {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || ("Erreur API " + response.status));
        }

        return response.json();
    }

    async function submitFlow() {
        setMessage("Envoi en cours...");

        const demandeur = {
            nom: getValue("nom"),
            prenom: getValue("prenom") || null,
            dateNaissance: getValue("dateNaissance"),
            nomJeuneFille: getValue("nomJeuneFille") || null,
            adresseMada: getValue("adresseMada") || null,
            telephone: getValue("telephone"),
            email: getValue("email") || null,
            idNationalite: toNumber("idNationalite"),
            idStatutFamilial: toNumber("idStatutFamilial")
        };

        const demandeurResponse = await postJson("/api/demandeurs", demandeur);
        state.idDemandeur = demandeurResponse.idDemandeur;

        const passport = {
            numero: getValue("numero"),
            dateDelivrance: getValue("dateDelivrance") || null,
            dateExpiration: getValue("dateExpiration") || null
        };

        const passportResponse = await postJson("/api/demandeurs/" + state.idDemandeur + "/passports", passport);
        state.idPassport = passportResponse.idPassport;

        const visa = {
            referenceVisa: getValue("referenceVisa"),
            natureVisa: getValue("natureVisa") || null,
            dateEntreeMada: getValue("dateEntreeMada"),
            lieuEntreeMada: getValue("lieuEntreeMada") || null,
            dateSortie: getValue("dateSortie") || null
        };

        const visaResponse = await postJson("/api/passports/" + state.idPassport + "/visas-transformables", visa);
        state.idVisaTransformable = visaResponse.id;

        const demande = {
            idDemandeur: state.idDemandeur,
            idPassport: state.idPassport,
            idVisaTransformable: state.idVisaTransformable,
            idTypeVisa: toNumber("idTypeVisa"),
            idTypeDemande: toNumber("idTypeDemande"),
            piecesJointes: state.piecesSelection
        };

        const soumission = await postJson("/api/demandes/nouveau-titre", demande);
        const query = new URLSearchParams({
            id: String(soumission.id || ""),
            statut: String(soumission.statut || ""),
            date: String(soumission.dateDemande || "")
        });
        window.location.href = "/demande/confirmation?" + query.toString();
    }

    btnPrev.addEventListener("click", () => {
        setMessage("");
        currentStep = Math.max(1, currentStep - 1);
        syncUi();
    });

    btnNext.addEventListener("click", () => {
        const error = validateStep(currentStep);
        if (error) {
            setMessage(error);
            return;
        }

        if (currentStep === 4) {
            buildPieces();
        }

        setMessage("");
        currentStep = Math.min(5, currentStep + 1);
        syncUi();
    });

    btnSubmit.addEventListener("click", async () => {
        const error = validateStep(5);
        if (error) {
            setMessage(error);
            return;
        }

        try {
            btnSubmit.disabled = true;
            await submitFlow();
        } catch (e) {
            setMessage("Echec de soumission: " + (e.message || "erreur inconnue"));
        } finally {
            btnSubmit.disabled = false;
        }
    });

    syncUi();
})();
