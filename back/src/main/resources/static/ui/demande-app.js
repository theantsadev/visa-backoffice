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

    const references = [
        { field: "idNationalite", url: "/api/nationalites" },
        { field: "idStatutFamilial", url: "/api/statuts-familiaux" },
        { field: "idTypeVisa", url: "/api/types-visa" },
        { field: "idTypeDemande", url: "/api/types-demande" }
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
            const missing = state.piecesSelection.filter((p) => p.obligatoire && !p.fournie);
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

    function getCategorieLabel(categorie) {
        if (categorie === "COMMUNE") {
            return "Commune";
        }
        if (categorie === "SPECIFIQUE_VISA") {
            return "Specifique visa";
        }
        if (categorie === "SPECIFIQUE_DEMANDE") {
            return "Specifique demande";
        }
        if (categorie === "SPECIFIQUE_VISA_DEMANDE") {
            return "Specifique visa + demande";
        }
        return "Obligatoire";
    }

    function fillSelect(fieldName, items) {
        const select = form.querySelector("[name='" + fieldName + "']");
        if (!select) {
            return;
        }

        const first = select.querySelector("option[value='']");
        select.innerHTML = "";
        if (first) {
            select.appendChild(first);
        }

        items.forEach((item) => {
            const option = document.createElement("option");
            option.value = String(item.id);
            option.textContent = item.libelle || item.nom || ("Valeur " + item.id);
            select.appendChild(option);
        });
    }

    async function loadReferences() {
        const results = await Promise.all(references.map((config) => getJson(config.url)));
        references.forEach((config, index) => {
            fillSelect(config.field, Array.isArray(results[index]) ? results[index] : []);
        });
    }

    async function buildPieces() {
        const idTypeVisa = toNumber("idTypeVisa");
        const idTypeDemande = toNumber("idTypeDemande");
        if (!idTypeVisa || !idTypeDemande) {
            throw new Error("Type de visa et type de demande requis pour charger les pieces.");
        }

        const rows = await getJson("/api/pieces-a-fournir?typeVisa=" + idTypeVisa);

        state.piecesSelection = rows.map((piece) => ({
            idPieceAFournir: piece.idPieceAFournir,
            fournie: false,
            obligatoire: Boolean(piece.obligatoire)
        }));

        piecesContainer.innerHTML = "";
        if (!rows.length) {
            piecesContainer.textContent = "Aucune piece obligatoire trouvee pour cette combinaison.";
            return;
        }

        rows.forEach((piece) => {
            const wrapper = document.createElement("label");
            wrapper.className = "piece-row";

            const left = document.createElement("div");
            left.textContent = piece.nom;

            const tag = document.createElement("span");
            tag.className = "piece-tag";
            tag.textContent = getCategorieLabel(piece.categorie);
            left.appendChild(tag);

            const check = document.createElement("input");
            check.type = "checkbox";
            check.addEventListener("change", () => {
                const item = state.piecesSelection.find((p) => p.idPieceAFournir === piece.idPieceAFournir);
                if (item) {
                    item.fournie = check.checked;
                }
            });

            wrapper.appendChild(left);
            wrapper.appendChild(check);
            piecesContainer.appendChild(wrapper);
        });
    }

    async function getJson(url) {
        const response = await fetch(url, {
            method: "GET"
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || ("Erreur API " + response.status));
        }

        return response.json();
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

    btnNext.addEventListener("click", async () => {
        const error = validateStep(currentStep);
        if (error) {
            setMessage(error);
            return;
        }

        if (currentStep === 4) {
            try {
                await buildPieces();
            } catch (e) {
                setMessage("Impossible de charger les pieces: " + (e.message || "erreur inconnue"));
                return;
            }
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

    async function init() {
        syncUi();
        try {
            await loadReferences();
        } catch (e) {
            setMessage("Impossible de charger les donnees de reference: " + (e.message || "erreur inconnue"));
        }
    }

    init();
})();
