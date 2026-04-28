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
    const quickDemandeur = document.getElementById("quickDemandeur");
    const quickPassport = document.getElementById("quickPassport");
    const quickVisa = document.getElementById("quickVisa");
    const sansDonnees = document.getElementById("sansDonnees");
    const sansDonneesWrapper = document.getElementById("sansDonneesWrapper");
    const transfertNote = document.getElementById("transfertNote");
    const confirmationUrl = form.dataset.confirmationUrl || "/demande/confirmation";
    const AUTOSAVE_KEY = "demande-nouveau-autosave-v1";
    const AUTOSAVE_STEP_MAX = 5;

    let currentStep = 1;
    const state = {
        idDemandeur: null,
        idPassport: null,
        idVisaTransformable: null,
        idTypeVisa: null,
        idTypeDemande: null,
        piecesSelection: [],
        quick: {
            demandeurs: [],
            passports: [],
            visas: []
        }
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

    function safeStorageAvailable() {
        try {
            return typeof window !== "undefined" && window.localStorage;
        } catch (e) {
            return false;
        }
    }

    function saveAutosave() {
        if (!safeStorageAvailable()) {
            return;
        }

        const fields = {};
        const namedInputs = form.querySelectorAll("input[name], select[name], textarea[name]");
        namedInputs.forEach((el) => {
            fields[el.name] = el.value;
        });

        const payload = {
            step: currentStep,
            fields: fields,
            piecesSelection: state.piecesSelection,
            quickSelection: {
                idDemandeur: quickDemandeur ? quickDemandeur.value : "",
                idPassport: quickPassport ? quickPassport.value : "",
                idVisa: quickVisa ? quickVisa.value : ""
            }
        };

        localStorage.setItem(AUTOSAVE_KEY, JSON.stringify(payload));
    }

    function restoreAutosave() {
        if (!safeStorageAvailable()) {
            return false;
        }

        const raw = localStorage.getItem(AUTOSAVE_KEY);
        if (!raw) {
            return false;
        }

        try {
            const payload = JSON.parse(raw);
            const fields = payload.fields || {};

            Object.keys(fields).forEach((name) => {
                const el = form.querySelector("[name='" + name + "']");
                if (el) {
                    el.value = fields[name] == null ? "" : String(fields[name]);
                }
            });

            if (Array.isArray(payload.piecesSelection)) {
                state.piecesSelection = payload.piecesSelection.map((item) => ({
                    idPieceAFournir: item.idPieceAFournir,
                    selected: Boolean(item.selected || item.fournie),
                    obligatoire: Boolean(item.obligatoire)
                }));
            }

            if (payload.step) {
                const step = Number(payload.step);
                if (!Number.isNaN(step) && step >= 1) {
                    currentStep = Math.min(AUTOSAVE_STEP_MAX, step);
                }
            }

            return true;
        } catch (e) {
            localStorage.removeItem(AUTOSAVE_KEY);
            return false;
        }
    }

    function clearAutosave() {
        if (!safeStorageAvailable()) {
            return;
        }

        localStorage.removeItem(AUTOSAVE_KEY);
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
            if (isSansDonneesChecked()) {
                return null;
            }
            return required("[name='referenceVisa']", "Reference visa")
                || required("[name='dateEntreeMada']", "Date entree Madagascar");
        }
        if (step === 4) {
            return required("[name='idTypeVisa']", "Type de visa")
                || required("[name='idTypeDemande']", "Type de demande");
        }
        if (step === 5) {
            const missing = state.piecesSelection.filter((p) => p.obligatoire && !p.selected);
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

    function getIdTypeDemande() {
        return toNumber("idTypeDemande");
    }

    function isSansDonneesChecked() {
        return Boolean(sansDonnees && sansDonnees.checked);
    }

    function isDemandeDuplicata() {
        return getIdTypeDemande() === 2;
    }

    function isDemandeTransfert() {
        return getIdTypeDemande() === 3;
    }

    function updateDemandeTypeUi() {
        const showSansDonnees = isDemandeDuplicata() || isDemandeTransfert();
        if (sansDonneesWrapper) {
            sansDonneesWrapper.hidden = !showSansDonnees;
        }

        if (!showSansDonnees && sansDonnees) {
            sansDonnees.checked = false;
        }

        if (transfertNote) {
            transfertNote.hidden = !isDemandeTransfert();
        }

        if (quickVisa) {
            quickVisa.disabled = isSansDonneesChecked();
            if (isSansDonneesChecked()) {
                quickVisa.value = "";
            }
        }
    }

    function getCategorieLabel(categorie) {
        if (categorie === "COMMUNE") {
            return "Commune";
        }

        return "Spécifique";
    }

    function getTypeLabel(obligatoire) {
        if (obligatoire === true) {
            return "Obligatoire";
        }

        return "Facultatif";
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

    function fillQuickSelect(select, items, valueKey) {
        if (!select) {
            return;
        }

        const previous = String(select.value || "");
        const first = document.createElement("option");
        first.value = "";
        first.textContent = "Selectionner";

        select.innerHTML = "";
        select.appendChild(first);

        items.forEach((item) => {
            const option = document.createElement("option");
            option.value = String(item[valueKey]);
            option.textContent = item.label || ("Valeur " + item[valueKey]);
            select.appendChild(option);
        });

        const found = items.some((item) => String(item[valueKey]) === previous);
        if (found) {
            select.value = previous;
        }
    }

    function applyDemandeurToForm(item) {
        if (!item) {
            return;
        }

        const mapping = {
            nom: item.nom,
            prenom: item.prenom,
            dateNaissance: item.dateNaissance,
            nomJeuneFille: item.nomJeuneFille,
            adresseMada: item.adresseMada,
            telephone: item.telephone,
            email: item.email,
            idNationalite: item.idNationalite,
            idStatutFamilial: item.idStatutFamilial
        };

        Object.keys(mapping).forEach((name) => {
            const el = form.querySelector("[name='" + name + "']");
            if (el && mapping[name] != null) {
                el.value = String(mapping[name]);
            }
        });
    }

    function applyPassportToForm(item) {
        if (!item) {
            return;
        }

        const mapping = {
            numero: item.numero,
            dateDelivrance: item.dateDelivrance,
            dateExpiration: item.dateExpiration
        };

        Object.keys(mapping).forEach((name) => {
            const el = form.querySelector("[name='" + name + "']");
            if (el && mapping[name] != null) {
                el.value = String(mapping[name]);
            }
        });
    }

    function applyVisaToForm(item) {
        if (!item) {
            return;
        }

        const mapping = {
            referenceVisa: item.referenceVisa,
            natureVisa: item.natureVisa,
            dateEntreeMada: item.dateEntreeMada,
            lieuEntreeMada: item.lieuEntreeMada,
            dateSortie: item.dateSortie
        };

        Object.keys(mapping).forEach((name) => {
            const el = form.querySelector("[name='" + name + "']");
            if (el && mapping[name] != null) {
                el.value = String(mapping[name]);
            }
        });
    }

    async function loadQuickDemandeurs() {
        if (!quickDemandeur) {
            return;
        }

        const rows = await getJson("/api/demandeurs-rapides");
        state.quick.demandeurs = Array.isArray(rows) ? rows : [];
        fillQuickSelect(quickDemandeur, state.quick.demandeurs, "idDemandeur");
    }

    async function loadQuickPassports(idDemandeur) {
        if (!quickPassport) {
            return;
        }

        if (!idDemandeur) {
            state.quick.passports = [];
            fillQuickSelect(quickPassport, [], "idPassport");
            quickPassport.disabled = true;
            return;
        }

        const rows = await getJson("/api/passports-rapides?idDemandeur=" + Number(idDemandeur));
        state.quick.passports = Array.isArray(rows) ? rows : [];
        fillQuickSelect(quickPassport, state.quick.passports, "idPassport");
        quickPassport.disabled = false;
    }

    async function loadQuickVisas(idDemandeur, idPassport) {
        if (!quickVisa) {
            return;
        }

        if (!idDemandeur || !idPassport) {
            state.quick.visas = [];
            fillQuickSelect(quickVisa, [], "id");
            quickVisa.disabled = true;
            return;
        }

        const rows = await getJson("/api/visas-transformables-rapides?idDemandeur=" + Number(idDemandeur)
            + "&idPassport=" + Number(idPassport));
        state.quick.visas = Array.isArray(rows) ? rows : [];
        fillQuickSelect(quickVisa, state.quick.visas, "id");
        quickVisa.disabled = false;
    }

    async function restoreQuickSelections() {
        if (!safeStorageAvailable()) {
            return;
        }

        const raw = localStorage.getItem(AUTOSAVE_KEY);
        if (!raw) {
            return;
        }

        try {
            const payload = JSON.parse(raw);
            const quickSelection = payload.quickSelection || {};
            const idDemandeur = String(quickSelection.idDemandeur || "");
            const idPassport = String(quickSelection.idPassport || "");
            const idVisa = String(quickSelection.idVisa || "");

            if (quickDemandeur && idDemandeur) {
                quickDemandeur.value = idDemandeur;
                const demandeur = state.quick.demandeurs.find((row) => String(row.idDemandeur) === idDemandeur);
                if (demandeur) {
                    applyDemandeurToForm(demandeur);
                    await loadQuickPassports(demandeur.idDemandeur);
                }
            }

            if (quickPassport && idPassport) {
                quickPassport.value = idPassport;
                const passport = state.quick.passports.find((row) => String(row.idPassport) === idPassport);
                if (passport && quickDemandeur && quickDemandeur.value) {
                    applyPassportToForm(passport);
                    await loadQuickVisas(Number(quickDemandeur.value), passport.idPassport);
                }
            }

            if (quickVisa && idVisa) {
                quickVisa.value = idVisa;
                const visa = state.quick.visas.find((row) => String(row.id) === idVisa);
                if (visa) {
                    applyVisaToForm(visa);
                }
            }
        } catch (e) {
            // ignore invalid autosave payload
        }
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

        const rows = await getJson("/api/pieces-a-fournir?typeVisa=" + idTypeVisa
            + "&typeDemande=" + idTypeDemande);

        const previousSelection = new Map(
            state.piecesSelection.map((item) => [item.idPieceAFournir, Boolean(item.selected)])
        );

        state.piecesSelection = rows.map((piece) => ({
            idPieceAFournir: piece.idPieceAFournir,
            selected: Boolean(previousSelection.get(piece.idPieceAFournir)),
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
            tag.textContent = getCategorieLabel(piece.categorie) + ' | ' + getTypeLabel(piece.obligatoire);
            left.appendChild(tag);

            const check = document.createElement("input");
            check.type = "checkbox";
            check.checked = Boolean(previousSelection.get(piece.idPieceAFournir));
            check.addEventListener("change", () => {
                const item = state.piecesSelection.find((p) => p.idPieceAFournir === piece.idPieceAFournir);
                if (item) {
                    item.selected = check.checked;
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
            throw await buildApiError(response);
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
            throw await buildApiError(response);
        }

        return response.json();
    }

    async function buildApiError(response) {
        let text = "";
        try {
            text = await response.text();
        } catch (e) {
            text = "";
        }

        if (!text) {
            return new Error("Erreur API " + response.status);
        }

        try {
            const data = JSON.parse(text);
            if (data && typeof data === "object") {
                const code = data.code ? ("[" + data.code + "] ") : "";
                const message = data.message || data.error || ("Erreur API " + response.status);
                return new Error(code + message);
            }
        } catch (e) {
            // response body is not JSON
        }

        return new Error(text);
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

        const idTypeVisa = toNumber("idTypeVisa");
        const idTypeDemande = toNumber("idTypeDemande");
        const sansDonneesMode = isSansDonneesChecked();
        const piecesCible = state.piecesSelection
            .filter((p) => p.selected)
            .map((p) => ({ idPieceAFournir: p.idPieceAFournir }));

        let soumission;

        if (sansDonneesMode) {
            const demandeNouveauTitre = {
                idDemandeur: state.idDemandeur,
                idPassport: state.idPassport,
                idVisaTransformable: null,
                idTypeVisa: idTypeVisa,
                idTypeDemande: 1,
                piecesJointes: []
            };

            if (idTypeDemande === 2) {
                soumission = await postJson("/api/demandes/duplicata/sans-donnees", {
                    demandeNouveauTitre: demandeNouveauTitre,
                    piecesCible: piecesCible
                });
            } else if (idTypeDemande === 3) {
                soumission = await postJson("/api/demandes/transfert/sans-donnees", {
                    demandeNouveauTitre: demandeNouveauTitre,
                    idPassportNouveau: state.idPassport,
                    piecesCible: piecesCible
                });
            } else {
                throw new Error("Le type de demande selectionne n'est pas compatible avec ce mode.");
            }
        } else {
            if (idTypeDemande !== 1) {
                throw new Error("Veuillez activer 'sans donnees anterieures' pour ce type de demande.");
            }

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
                idTypeVisa: idTypeVisa,
                idTypeDemande: idTypeDemande,
                piecesJointes: piecesCible
            };

            soumission = await postJson("/api/demandes/nouveau-titre", demande);
        }
        const query = new URLSearchParams({
            id: String(soumission.id || ""),
            statut: String(soumission.statut || ""),
            date: String(soumission.dateDemande || "")
        });
        clearAutosave();
        window.location.href = confirmationUrl + "?" + query.toString();
    }

    async function goToStep(targetStep) {
        const nextStep = Number(targetStep);
        if (Number.isNaN(nextStep) || nextStep < 1 || nextStep > AUTOSAVE_STEP_MAX) {
            return;
        }

        if (nextStep > currentStep) {
            for (let step = currentStep; step < nextStep; step += 1) {
                const error = validateStep(step);
                if (error) {
                    setMessage(error);
                    return;
                }
                if (step === 4) {
                    try {
                        await buildPieces();
                    } catch (e) {
                        setMessage("Impossible de charger les pieces: " + (e.message || "erreur inconnue"));
                        return;
                    }
                }
            }
        }

        currentStep = nextStep;
        setMessage("");
        syncUi();
        saveAutosave();
    }

    btnPrev.addEventListener("click", () => {
        setMessage("");
        currentStep = Math.max(1, currentStep - 1);
        syncUi();
        saveAutosave();
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
        saveAutosave();
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

    steps.forEach((stepEl) => {
        const stepValue = Number(stepEl.dataset.step);
        if (Number.isNaN(stepValue)) {
            return;
        }

        stepEl.addEventListener("click", async () => {
            await goToStep(stepValue);
        });

        stepEl.addEventListener("keydown", async (event) => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                await goToStep(stepValue);
            }
        });
    });

    if (quickDemandeur) {
        quickDemandeur.addEventListener("change", async () => {
            const idDemandeur = quickDemandeur.value ? Number(quickDemandeur.value) : null;
            const demandeur = state.quick.demandeurs.find((row) => row.idDemandeur === idDemandeur);
            applyDemandeurToForm(demandeur || null);

            quickPassport.value = "";
            quickVisa.value = "";
            await loadQuickPassports(idDemandeur);
            await loadQuickVisas(null, null);
            saveAutosave();
        });
    }

    if (quickPassport) {
        quickPassport.addEventListener("change", async () => {
            const idPassport = quickPassport.value ? Number(quickPassport.value) : null;
            const idDemandeur = quickDemandeur && quickDemandeur.value ? Number(quickDemandeur.value) : null;
            const passport = state.quick.passports.find((row) => row.idPassport === idPassport);
            applyPassportToForm(passport || null);

            quickVisa.value = "";
            await loadQuickVisas(idDemandeur, idPassport);
            saveAutosave();
        });
    }

    if (quickVisa) {
        quickVisa.addEventListener("change", () => {
            const idVisa = quickVisa.value ? Number(quickVisa.value) : null;
            const visa = state.quick.visas.find((row) => row.id === idVisa);
            applyVisaToForm(visa || null);
            saveAutosave();
        });
    }

    form.addEventListener("input", () => {
        saveAutosave();
    });

    form.addEventListener("change", () => {
        saveAutosave();
    });

    async function init() {
        restoreAutosave();
        syncUi();
        try {
            await loadReferences();
            await loadQuickDemandeurs();
            await restoreQuickSelections();

            updateDemandeTypeUi();

            if (currentStep === 5 && state.piecesSelection.length) {
                await buildPieces();
            }
        } catch (e) {
            setMessage("Impossible de charger les donnees de reference: " + (e.message || "erreur inconnue"));
        }
    }

    const typeDemandeSelect = form.querySelector("[name='idTypeDemande']");
    if (typeDemandeSelect) {
        typeDemandeSelect.addEventListener("change", () => {
            updateDemandeTypeUi();
            saveAutosave();
        });
    }

    if (sansDonnees) {
        sansDonnees.addEventListener("change", () => {
            updateDemandeTypeUi();
            saveAutosave();
        });
    }

    init();
})();
