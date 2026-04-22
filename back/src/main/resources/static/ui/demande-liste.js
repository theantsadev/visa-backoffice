(function () {
    const listContainer = document.getElementById("demandeList");
    const listFeedback = document.getElementById("listFeedback");
    const searchInput = document.getElementById("searchDemande");
    const detailPlaceholder = document.getElementById("detailPlaceholder");
    const detailContent = document.getElementById("detailContent");

    if (!listContainer || !detailContent || !detailPlaceholder || !searchInput) {
        return;
    }

    let demandes = [];
    let demandesFiltrees = [];
    let selectedId = null;

    function setFeedback(message, ok) {
        listFeedback.textContent = message || "";
        listFeedback.classList.toggle("ok", Boolean(ok));
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

    function renderList() {
        listContainer.innerHTML = "";

        if (demandesFiltrees.length === 0) {
            const empty = document.createElement("div");
            empty.className = "detail-placeholder";
            empty.textContent = "Aucune demande ne correspond a votre recherche.";
            listContainer.appendChild(empty);
            return;
        }

        demandesFiltrees.forEach((demande) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "demande-row" + (demande.id === selectedId ? " is-active" : "");

            const top = document.createElement("div");
            top.className = "demande-row-top";

            const title = document.createElement("strong");
            title.textContent = safe(demande.nomDemandeur);

            const idTag = document.createElement("span");
            idTag.className = "id-chip";
            idTag.textContent = "ID " + safe(demande.id);

            const titleWrap = document.createElement("div");
            titleWrap.className = "demande-row-title";
            titleWrap.appendChild(title);
            titleWrap.appendChild(idTag);

            const status = document.createElement("span");
            status.className = "status-chip";
            status.textContent = safe(demande.statut);

            top.appendChild(titleWrap);
            top.appendChild(status);

            const meta = document.createElement("div");
            meta.className = "demande-row-meta";
            meta.textContent = safe(demande.typeVisa) + " | " + safe(demande.typeDemande);

            const date = document.createElement("div");
            date.className = "demande-row-date";
            date.textContent = formatDateTime(demande.dateDemande);

            button.appendChild(top);
            button.appendChild(meta);
            button.appendChild(date);

            button.addEventListener("click", function () {
                loadDetail(demande.id);
            });

            listContainer.appendChild(button);
        });
    }

    function filterList(term) {
        const normalized = String(term || "").toLowerCase().trim();

        if (!normalized) {
            demandesFiltrees = demandes.slice();
            renderList();
            return;
        }

        demandesFiltrees = demandes.filter((demande) => {
            const haystack = [
                demande.id,
                demande.nomDemandeur,
                demande.typeVisa,
                demande.typeDemande,
                demande.statut,
                demande.dateDemande
            ]
                .filter(Boolean)
                .join(" ")
                .toLowerCase();

            return haystack.includes(normalized);
        });

        renderList();
    }

    function createDetailSection(title, rows) {
        const section = document.createElement("article");
        section.className = "detail-block";

        const heading = document.createElement("h3");
        heading.textContent = title;
        section.appendChild(heading);

        const grid = document.createElement("div");
        grid.className = "detail-grid";

        rows.forEach(function (row) {
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

    function renderDetail(detail) {
        detailPlaceholder.hidden = true;
        detailContent.hidden = false;
        detailContent.innerHTML = "";

        const pieces = Array.isArray(detail.piecesJointes) ? detail.piecesJointes : [];

        detailContent.appendChild(createDetailSection("Demande", [
            { label: "ID", value: detail.id },
            { label: "Statut", value: detail.statut },
            { label: "Type visa", value: detail.typeVisa },
            { label: "Type demande", value: detail.typeDemande },
            { label: "Date demande", value: formatDateTime(detail.dateDemande) }
        ]));

        detailContent.appendChild(createDetailSection("Demandeur", [
            { label: "ID demandeur", value: detail.demandeur && detail.demandeur.idDemandeur },
            { label: "Nom", value: detail.demandeur && detail.demandeur.nom },
            { label: "Prenom", value: detail.demandeur && detail.demandeur.prenom },
            { label: "Date de naissance", value: detail.demandeur && detail.demandeur.dateNaissance },
            { label: "Telephone", value: detail.demandeur && detail.demandeur.telephone },
            { label: "Email", value: detail.demandeur && detail.demandeur.email }
        ]));

        detailContent.appendChild(createDetailSection("Passeport", [
            { label: "ID passeport", value: detail.passport && detail.passport.idPassport },
            { label: "Numero", value: detail.passport && detail.passport.numero },
            { label: "Date de delivrance", value: detail.passport && detail.passport.dateDelivrance },
            { label: "Date d'expiration", value: detail.passport && detail.passport.dateExpiration }
        ]));

        detailContent.appendChild(createDetailSection("Visa transformable", [
            { label: "ID visa", value: detail.visaTransformable && detail.visaTransformable.idVisaTransformable },
            { label: "Reference", value: detail.visaTransformable && detail.visaTransformable.referenceVisa },
            { label: "Date entree", value: detail.visaTransformable && detail.visaTransformable.dateEntreeMada },
            { label: "Lieu entree", value: detail.visaTransformable && detail.visaTransformable.lieuEntreeMada },
            { label: "Date sortie", value: detail.visaTransformable && detail.visaTransformable.dateSortie }
        ]));

        const pieceSection = document.createElement("article");
        pieceSection.className = "detail-block";

        const pieceTitle = document.createElement("h3");
        pieceTitle.textContent = "Pieces jointes";
        pieceSection.appendChild(pieceTitle);

        if (pieces.length === 0) {
            const hint = document.createElement("p");
            hint.className = "hint";
            hint.textContent = "Aucune piece jointe enregistree.";
            pieceSection.appendChild(hint);
        } else {
            const list = document.createElement("ul");
            list.className = "piece-detail-list";

            pieces.forEach(function (piece) {
                const item = document.createElement("li");
                item.className = "piece-detail-row";

                const left = document.createElement("div");
                const name = document.createElement("strong");
                name.textContent = safe(piece.nomPiece);
                const info = document.createElement("span");
                info.textContent = "ID piece: " + safe(piece.idPieceAFournir);

                left.appendChild(name);
                left.appendChild(info);

                const state = document.createElement("span");
                state.className = "piece-state " + (piece.fournie ? "is-ok" : "is-missing");
                state.textContent = piece.fournie ? "Fournie" : "Non fournie";

                item.appendChild(left);
                item.appendChild(state);
                list.appendChild(item);
            });

            pieceSection.appendChild(list);
        }

        detailContent.appendChild(pieceSection);
    }

    async function loadDetail(idDemande) {
        selectedId = idDemande;
        renderList();
        detailPlaceholder.hidden = false;
        detailContent.hidden = true;
        detailPlaceholder.textContent = "Chargement du detail...";

        try {
            const response = await fetch("/api/demandes/" + idDemande);
            if (!response.ok) {
                throw new Error("Erreur API " + response.status);
            }

            const detail = await response.json();
            renderDetail(detail);
        } catch (error) {
            detailPlaceholder.hidden = false;
            detailContent.hidden = true;
            detailPlaceholder.textContent = "Impossible de charger le detail de la demande.";
        }
    }

    async function loadList() {
        setFeedback("Chargement de la liste...");
        try {
            const response = await fetch("/api/demandes");
            if (!response.ok) {
                throw new Error("Erreur API " + response.status);
            }

            demandes = await response.json();
            demandesFiltrees = demandes.slice();
            renderList();
            setFeedback("Liste chargee.", true);

            if (demandesFiltrees.length > 0) {
                loadDetail(demandesFiltrees[0].id);
            }
        } catch (error) {
            setFeedback("Impossible de charger la liste des demandes.");
            listContainer.innerHTML = "";
        }
    }

    searchInput.addEventListener("input", function () {
        filterList(searchInput.value);
    });

    loadList();
})();
