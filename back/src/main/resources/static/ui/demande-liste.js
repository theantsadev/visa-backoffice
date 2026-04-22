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

            const status = document.createElement("span");
            status.className = "status-chip";
            status.textContent = safe(demande.statut);

            top.appendChild(title);
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

    function sectionHtml(title, rows) {
        const lines = rows
            .map(function (row) {
                return "<div><span>" + row.label + "</span><strong>" + safe(row.value) + "</strong></div>";
            })
            .join("");

        return "<article class=\"detail-block\"><h3>" + title + "</h3><div class=\"detail-grid\">" + lines + "</div></article>";
    }

    function renderDetail(detail) {
        detailPlaceholder.hidden = true;
        detailContent.hidden = false;

        const pieces = Array.isArray(detail.piecesJointes) ? detail.piecesJointes : [];
        const piecesRows = pieces.length === 0
            ? "<p class=\"hint\">Aucune piece jointe enregistree.</p>"
            : pieces
                .map(function (piece) {
                    const stateClass = piece.fournie ? "is-ok" : "is-missing";
                    const stateLabel = piece.fournie ? "Fournie" : "Non fournie";
                    return "<li class=\"piece-detail-row\">"
                        + "<div><strong>" + safe(piece.nomPiece) + "</strong><span>ID piece: " + safe(piece.idPieceAFournir) + "</span></div>"
                        + "<span class=\"piece-state " + stateClass + "\">" + stateLabel + "</span>"
                        + "</li>";
                })
                .join("");

        detailContent.innerHTML = ""
            + sectionHtml("Demande", [
                { label: "ID", value: detail.id },
                { label: "Statut", value: detail.statut },
                { label: "Type visa", value: detail.typeVisa },
                { label: "Type demande", value: detail.typeDemande },
                { label: "Date demande", value: formatDateTime(detail.dateDemande) }
            ])
            + sectionHtml("Demandeur", [
                { label: "ID demandeur", value: detail.demandeur && detail.demandeur.idDemandeur },
                { label: "Nom", value: detail.demandeur && detail.demandeur.nom },
                { label: "Prenom", value: detail.demandeur && detail.demandeur.prenom },
                { label: "Date naissance", value: detail.demandeur && detail.demandeur.dateNaissance },
                { label: "Telephone", value: detail.demandeur && detail.demandeur.telephone },
                { label: "Email", value: detail.demandeur && detail.demandeur.email }
            ])
            + sectionHtml("Passeport", [
                { label: "ID passeport", value: detail.passport && detail.passport.idPassport },
                { label: "Numero", value: detail.passport && detail.passport.numero },
                { label: "Date delivrance", value: detail.passport && detail.passport.dateDelivrance },
                { label: "Date expiration", value: detail.passport && detail.passport.dateExpiration }
            ])
            + sectionHtml("Visa transformable", [
                { label: "ID visa", value: detail.visaTransformable && detail.visaTransformable.idVisaTransformable },
                { label: "Reference", value: detail.visaTransformable && detail.visaTransformable.referenceVisa },
                { label: "Date entree", value: detail.visaTransformable && detail.visaTransformable.dateEntreeMada },
                { label: "Lieu entree", value: detail.visaTransformable && detail.visaTransformable.lieuEntreeMada },
                { label: "Date sortie", value: detail.visaTransformable && detail.visaTransformable.dateSortie }
            ])
            + "<article class=\"detail-block\"><h3>Pieces jointes</h3><ul class=\"piece-detail-list\">" + piecesRows + "</ul></article>";
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
