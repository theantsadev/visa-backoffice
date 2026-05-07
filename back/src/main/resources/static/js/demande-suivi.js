(function () {
    const feedbackEl = document.getElementById("suiviFeedback");
    const contentEl = document.getElementById("suiviContent");

    if (!feedbackEl || !contentEl) {
        return;
    }

    const numeroId = decodeURIComponent(window.location.pathname.split("/").filter(Boolean).pop() || "");

    function setFeedback(message, isError) {
        feedbackEl.textContent = message || "";
        feedbackEl.classList.toggle("ok", !isError && Boolean(message));
    }

    function formatDateTime(value) {
        if (!value) {
            return "-";
        }

        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return new Intl.DateTimeFormat("fr-FR", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }).format(date);
    }

    function safe(value) {
        return value == null || String(value).trim() === "" ? "-" : String(value);
    }

    function createBlock(title, rows) {
        const section = document.createElement("article");
        section.className = "detail-block";

        const heading = document.createElement("h3");
        heading.textContent = title;
        section.appendChild(heading);

        const grid = document.createElement("div");
        grid.className = "detail-grid";

        rows.forEach((row) => {
            const item = document.createElement("div");
            item.className = "detail-item";

            const label = document.createElement("span");
            label.className = "detail-label";
            label.textContent = row.label;

            const value = document.createElement("strong");
            value.className = "detail-value";
            value.textContent = safe(row.value);

            item.appendChild(label);
            item.appendChild(value);
            grid.appendChild(item);
        });

        section.appendChild(grid);
        return section;
    }

    function renderSuiviItem(suivi) {
        const item = document.createElement("li");
        item.className = "piece-detail-row suivi-row";

        const left = document.createElement("div");
        left.className = "suivi-left";
        const title = document.createElement("strong");
        const reference = suivi.demande.numero || suivi.demande.id;
        title.textContent = "Demande #" + safe(reference);
        left.appendChild(title);

        const fields = document.createElement("div");
        fields.className = "suivi-fields";
        const rows = [
            { label: "Nom du demandeur", value: suivi.demande.nomDemandeur },
            { label: "Numero", value: reference },
            { label: "Type demande", value: suivi.demande.typeDemande },
            { label: "Type visa", value: suivi.demande.typeVisa },
            { label: "Statut", value: suivi.demande.statut },
            { label: "Date demande", value: formatDateTime(suivi.demande.dateDemande) }
        ];

        rows.forEach((row) => {
            const field = document.createElement("div");
            field.className = "suivi-field";

            const label = document.createElement("span");
            label.className = "detail-label";
            label.textContent = row.label;

            const value = document.createElement("strong");
            value.className = "detail-value";
            value.textContent = safe(row.value);

            field.appendChild(label);
            field.appendChild(value);
            fields.appendChild(field);
        });

        left.appendChild(fields);

        const toggle = document.createElement("button");
        toggle.type = "button";
        toggle.className = "btn btn-outline";
        toggle.textContent = "Afficher historique statut";
        toggle.setAttribute("aria-expanded", "false");

        const historiqueSection = renderHistorique(suivi.historiqueStatuts || []);
        historiqueSection.hidden = true;

        toggle.addEventListener("click", () => {
            const isHidden = historiqueSection.hidden;
            historiqueSection.hidden = !isHidden;
            toggle.textContent = isHidden ? "Masquer historique statut" : "Afficher historique statut";
            toggle.setAttribute("aria-expanded", String(isHidden));
        });

        item.appendChild(left);
        item.appendChild(toggle);
        item.appendChild(historiqueSection);

        return item;
    }

    function renderListeSuivi(listeSuivi) {
        const section = document.createElement("article");
        section.className = "detail-block";

        const heading = document.createElement("h3");
        heading.textContent = "Demandes associees";
        section.appendChild(heading);

        if (!listeSuivi.length) {
            const hint = document.createElement("p");
            hint.className = "hint";
            hint.textContent = "Aucune demande associee.";
            section.appendChild(hint);
            return section;
        }

        const list = document.createElement("ul");
        list.className = "piece-detail-list";

        listeSuivi.forEach((suivi) => {
            list.appendChild(renderSuiviItem(suivi));
        });

        section.appendChild(list);
        return section;
    }

    function renderHistorique(historique) {
        const section = document.createElement("article");
        section.className = "detail-block";

        const heading = document.createElement("h3");
        heading.textContent = "Historique des statuts";
        section.appendChild(heading);

        if (!historique.length) {
            const hint = document.createElement("p");
            hint.className = "hint";
            hint.textContent = "Aucun historique disponible.";
            section.appendChild(hint);
            return section;
        }

        const list = document.createElement("ul");
        list.className = "piece-detail-list";

        historique.forEach((itemHistorique) => {
            const item = document.createElement("li");
            item.className = "piece-detail-row";

            const left = document.createElement("div");
            const title = document.createElement("strong");
            title.textContent = safe(itemHistorique.statut);
            const date = document.createElement("span");
            date.textContent = formatDateTime(itemHistorique.dateHeureHistorique);

            left.appendChild(title);
            left.appendChild(date);

            const badge = document.createElement("span");
            badge.className = "piece-state is-ok";
            badge.textContent = "ID " + safe(itemHistorique.idStatut);

            item.appendChild(left);
            item.appendChild(badge);
            list.appendChild(item);
        });

        section.appendChild(list);
        return section;
    }

    async function loadSuivi() {
        if (!numeroId) {
            setFeedback("Identifiant de suivi manquant.", true);
            return;
        }

        try {
            const response = await fetch("/api/demandes/suivi/" + encodeURIComponent(numeroId));
            if (!response.ok) {
                throw new Error("Erreur API " + response.status);
            }

            const listeSuivi = await response.json();


            contentEl.innerHTML = "";
            // contentEl.appendChild(createBlock("Demande de reference", [
            //     { label: "ID recherche", value: suivi.numeroRecherche },
            //     { label: "Type de recherche", value: suivi.typeRecherche },
            //     { label: "ID demande", value: suivi.demandeReference && suivi.demandeReference.id },
            //     { label: "Statut", value: suivi.demandeReference && suivi.demandeReference.statut },
            //     { label: "Type demande", value: suivi.demandeReference && suivi.demandeReference.typeDemande },
            //     { label: "Type visa", value: suivi.demandeReference && suivi.demandeReference.typeVisa },
            //     { label: "Date demande", value: suivi.demandeReference && suivi.demandeReference.dateDemande }
            // ]));
            // contentEl.appendChild(createBlock("Demandeur", [
            //     { label: "ID demandeur", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.idDemandeur },
            //     { label: "Nom", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.nom },
            //     { label: "Prenom", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.prenom },
            //     { label: "Telephone", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.telephone },
            //     { label: "Email", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.email },
            //     { label: "Nationalite", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.idNationalite },
            //     { label: "Situation familiale", value: suivi.demandeReference && suivi.demandeReference.demandeur && suivi.demandeReference.demandeur.idStatutFamilial }
            // ]));
            contentEl.appendChild(renderListeSuivi(listeSuivi));
            // contentEl.appendChild(renderHistorique(historique));

            contentEl.hidden = false;
            setFeedback("Suivi charge.", false);
        } catch (error) {
            setFeedback("Impossible de charger le suivi.", true);
            contentEl.hidden = true;
        }
    }

    loadSuivi();
})();