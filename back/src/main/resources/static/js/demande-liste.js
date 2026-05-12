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

    function normalizeStatus(value) {
        return String(value || "")
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/[^a-z0-9]/g, "");
    }

    async function buildApiError(response) {
        let text = "";
        try {
            text = await response.text();
        } catch (error) {
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
        } catch (error) {
            // response body is not JSON
        }

        return new Error(text);
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

    function buildTrackingUrl(numeroId) {
        return "/demande/suivi/" + encodeURIComponent(numeroId);
    }

    function buildQrUrl(numeroId) {
        return "/api/demandes/" + encodeURIComponent(numeroId) + "/qr?size=220";
    }

    function createQrPanel(numeroId) {
        const panel = document.createElement("div");
        panel.className = "qr-panel";

        const copy = document.createElement("div");
        const label = document.createElement("span");
        label.className = "qr-label";
        label.textContent = "QR de suivi";

        const subtitle = document.createElement("p");
        subtitle.className = "subtitle";
        subtitle.textContent = "Scannez ce QR pour ouvrir la page de suivi de la demande.";

        copy.appendChild(label);
        copy.appendChild(subtitle);

        const image = document.createElement("img");
        image.className = "qr-image";
        image.alt = "QR code de suivi";
        image.loading = "lazy";
        image.src = buildQrUrl(numeroId);

        panel.appendChild(copy);
        panel.appendChild(image);
        return panel;
    }

    function renderDetail(detail) {
        detailPlaceholder.hidden = true;
        detailContent.hidden = false;
        detailContent.innerHTML = "";

        const pieces = Array.isArray(detail.piecesJointes) ? detail.piecesJointes : [];

        detailContent.appendChild(createDetailSection("Demande", [
            { label: "ID", value: detail.id },
            { label: "Numero", value: detail.numero },
            { label: "Statut", value: detail.statut },
            { label: "Type visa", value: detail.typeVisa },
            { label: "Type demande", value: detail.typeDemande },
            { label: "Date demande", value: formatDateTime(detail.dateDemande) }
        ]));

        const trackingId = detail.numero || detail.id;
        if (trackingId != null && String(trackingId).trim() !== "") {
            detailContent.appendChild(createQrPanel(trackingId));
        }

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

        const statut = normalizeStatus(detail.statut);
        const isEditable = statut === "dossiercree";
        const isFinal = statut === "visaaccorde" || statut === "visarejete";

        if (isEditable || !isFinal) {
            const actions = document.createElement("div");
            actions.className = "cta-row";

            const trackingLink = document.createElement("a");
            trackingLink.className = "btn btn-ghost";
            trackingLink.href = buildTrackingUrl(detail.numero || detail.id);
            trackingLink.textContent = "Suivi de la demande";
            actions.appendChild(trackingLink);

            if (isEditable) {
                const editLink = document.createElement("a");
                editLink.className = "btn btn-primary";
                editLink.href = "/demande/nouveau?editId=" + encodeURIComponent(detail.id);
                editLink.textContent = "Modifier le dossier";
                actions.appendChild(editLink);
            }

            const acceptButton = document.createElement("button");
            acceptButton.type = "button";
            acceptButton.className = "btn btn-primary";
            acceptButton.textContent = "Accepter";
            acceptButton.addEventListener("click", async () => {
                await updateAdminStatus(detail.id, "accepte");
            });

            const refuseButton = document.createElement("button");
            refuseButton.type = "button";
            refuseButton.className = "btn btn-outline";
            refuseButton.textContent = "Refuser";
            refuseButton.addEventListener("click", async () => {
                await updateAdminStatus(detail.id, "refuse");
            });

            actions.appendChild(acceptButton);
            actions.appendChild(refuseButton);
            detailContent.appendChild(actions);
        }

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
                state.className = "piece-state is-ok";
                if (piece.lien) {
                    const link = document.createElement("a");
                    link.className = "piece-link";
                    link.href = piece.lien;
                    link.target = "_blank";
                    link.rel = "noopener noreferrer";
                    link.textContent = "Ouvrir le fichier";
                    state.appendChild(link);
                } else {
                    state.textContent = "Piece jointe";
                }

                item.appendChild(left);
                item.appendChild(state);
                list.appendChild(item);
            });

            pieceSection.appendChild(list);
        }

        detailContent.appendChild(pieceSection);

        var photoSigSection = document.createElement("article");
        photoSigSection.className = "detail-block";

        var photoSigTitle = document.createElement("h3");
        photoSigTitle.textContent = "Photo et signature";
        photoSigSection.appendChild(photoSigTitle);

        var photoSig = detail.photoSignature || null;
        var hasPhoto = photoSig && photoSig.lienPhoto;
        var hasSignature = photoSig && photoSig.lienSignature;

        if (!hasPhoto && !hasSignature) {
            var emptyHint = document.createElement("p");
            emptyHint.className = "photo-sig-detail-empty";
            emptyHint.textContent = "Aucune photo ou signature enregistree pour cette demande.";
            photoSigSection.appendChild(emptyHint);
        } else {
            var photoSigGrid = document.createElement("div");
            photoSigGrid.className = "photo-sig-detail-grid";

            if (hasPhoto) {
                var photoCard = document.createElement("div");
                photoCard.className = "photo-sig-detail-card";
                var photoLabel = document.createElement("p");
                photoLabel.textContent = "Photo d'identite";
                var photoImg = document.createElement("img");
                photoImg.src = photoSig.lienPhoto;
                photoImg.alt = "Photo du demandeur";
                photoImg.loading = "lazy";
                photoCard.appendChild(photoLabel);
                photoCard.appendChild(photoImg);
                photoSigGrid.appendChild(photoCard);
            }

            if (hasSignature) {
                var sigCard = document.createElement("div");
                sigCard.className = "photo-sig-detail-card";
                var sigLabel = document.createElement("p");
                sigLabel.textContent = "Signature";
                var sigImg = document.createElement("img");
                sigImg.src = photoSig.lienSignature;
                sigImg.alt = "Signature du demandeur";
                sigImg.loading = "lazy";
                sigCard.appendChild(sigLabel);
                sigCard.appendChild(sigImg);
                photoSigGrid.appendChild(sigCard);
            }

            photoSigSection.appendChild(photoSigGrid);
        }

        detailContent.appendChild(photoSigSection);
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

    async function updateAdminStatus(idDemande, action) {
        try {
            setFeedback(action === "accepte" ? "Validation en cours..." : "Refus en cours...", false);

            const response = await fetch("/api/demandes/" + idDemande + "/" + action, {
                method: "POST"
            });

            if (!response.ok) {
                throw await buildApiError(response);
            }

            setFeedback(action === "accepte" ? "Demande acceptee." : "Demande refusee.", true);
            await loadDetail(idDemande);
        } catch (error) {
            setFeedback("Impossible de mettre a jour le statut: " + (error.message || "erreur inconnue"));
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
