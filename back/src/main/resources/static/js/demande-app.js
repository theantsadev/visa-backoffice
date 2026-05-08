(function () {
    const form = document.getElementById("demandeForm");
    if (!form) {
        return;
    }

    const stepItems = Array.from(document.querySelectorAll(".step"));
    const stepPanels = Array.from(document.querySelectorAll(".step-panel"));
    const statusMessageEl = document.getElementById("feedback");
    const piecesListEl = document.getElementById("piecesContainer");
    const previousStepButton = document.getElementById("btnPrev");
    const nextStepButton = document.getElementById("btnNext");
    const submitButton = document.getElementById("btnSubmit");
    const sansDonneesCheckbox = document.getElementById("sansDonnees");
    const sansDonneesBlock = document.getElementById("sansDonneesWrapper");
    const transferHelp = document.getElementById("transfertNote");
    const newPassportHelp = document.getElementById("nouveauPassportNote");
    const newPassportFields = document.getElementById("nouveauPassportFields");
    const confirmationUrl = form.dataset.confirmationUrl || "/demande/confirmation";
    const titleEl = document.getElementById("demandeTitle");
    const editId = new URLSearchParams(window.location.search).get("editId");
    const editMode = Boolean(editId);
    const photoVideo = document.getElementById("photoVideo");
    const photoCanvas = document.getElementById("photoCanvas");
    const signatureCanvas = document.getElementById("signatureCanvas");
    const startCameraButton = document.getElementById("btnStartCamera");
    const capturePhotoButton = document.getElementById("btnCapturePhoto");
    const retakePhotoButton = document.getElementById("btnRetakePhoto");
    const clearSignatureButton = document.getElementById("btnClearSignature");
    const photoStatusEl = document.getElementById("photoStatus");
    const signatureStatusEl = document.getElementById("signatureStatus");
    const photoSignatureExistingEl = document.getElementById("photoSignatureExisting");

    const demandContracts = window.DemandeContracts || {};
    const stepMax = demandContracts.stepMax || 5;
    const formModels = demandContracts.formModels || {};
    const stepContracts = demandContracts.stepContracts || {};

    const requestDraft = {
        editDetail: null,
        requestIds: {
            idDemandeur: null,
            idPassport: null,
            idVisaTransformable: null
        },
        requestSelection: {},
        pieces: [],
        photoSignature: {
            existingPhoto: "",
            existingSignature: "",
            photoBlob: null,
            signatureBlob: null,
            hasSignatureInk: false,
            cameraError: "",
            stream: null
        }
    };

    const referenceSources = [
        { fieldName: "idNationalite", url: "/api/nationalites" },
        { fieldName: "idStatutFamilial", url: "/api/statuts-familiaux" },
        { fieldName: "idTypeVisa", url: "/api/types-visa" },
        { fieldName: "idTypeDemande", url: "/api/types-demande" }
    ];

    let activeStep = 1;

    function setStatusMessage(message, isSuccess) {
        statusMessageEl.textContent = message || "";
        statusMessageEl.classList.toggle("ok", Boolean(isSuccess));
    }

    function setPhotoStatus(message) {
        if (!photoStatusEl) {
            return;
        }

        photoStatusEl.textContent = message || "";
    }

    function setSignatureStatus(message) {
        if (!signatureStatusEl) {
            return;
        }

        signatureStatusEl.textContent = message || "";
    }

    function stopCamera() {
        if (requestDraft.photoSignature.stream) {
            requestDraft.photoSignature.stream.getTracks().forEach((track) => track.stop());
            requestDraft.photoSignature.stream = null;
        }
    }

    async function startCamera() {
        if (!photoVideo) {
            return;
        }

        stopCamera();
        requestDraft.photoSignature.cameraError = "";
        setPhotoStatus("");

        try {
            const stream = await navigator.mediaDevices.getUserMedia({ video: true });
            requestDraft.photoSignature.stream = stream;
            photoVideo.srcObject = stream;
            photoVideo.hidden = false;
            if (photoCanvas) {
                photoCanvas.hidden = true;
            }
            setPhotoStatus("Camera active.");
        } catch (error) {
            requestDraft.photoSignature.cameraError = "Impossible d'acceder a la camera.";
            setPhotoStatus(requestDraft.photoSignature.cameraError);
        }
    }

    function showCapturedPhoto(canvas) {
        if (!photoVideo || !canvas) {
            return;
        }

        photoVideo.hidden = true;
        canvas.hidden = false;
        setPhotoStatus("Photo capturee.");
    }

    function resetPhotoCapture() {
        requestDraft.photoSignature.photoBlob = null;
        if (photoCanvas) {
            const ctx = photoCanvas.getContext("2d");
            if (ctx) {
                ctx.clearRect(0, 0, photoCanvas.width, photoCanvas.height);
            }
            photoCanvas.hidden = true;
        }
        if (photoVideo) {
            photoVideo.hidden = false;
        }
        setPhotoStatus("");
    }

    function resizeSignatureCanvas() {
        if (!signatureCanvas) {
            return;
        }

        const ratio = window.devicePixelRatio || 1;
        const rect = signatureCanvas.getBoundingClientRect();
        signatureCanvas.width = rect.width * ratio;
        signatureCanvas.height = rect.height * ratio;

        const ctx = signatureCanvas.getContext("2d");
        if (ctx) {
            ctx.setTransform(1, 0, 0, 1, 0, 0);
            ctx.scale(ratio, ratio);
            ctx.lineWidth = 2;
            ctx.lineCap = "round";
            ctx.strokeStyle = "#102027";
        }
    }

    function clearSignatureCanvas() {
        if (!signatureCanvas) {
            return;
        }

        const ctx = signatureCanvas.getContext("2d");
        if (ctx) {
            ctx.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
        }
        requestDraft.photoSignature.hasSignatureInk = false;
        requestDraft.photoSignature.signatureBlob = null;
        setSignatureStatus("");
    }

    function isSignatureBlank() {
        if (!signatureCanvas) {
            return true;
        }

        const ctx = signatureCanvas.getContext("2d");
        if (!ctx) {
            return true;
        }

        const width = signatureCanvas.width;
        const height = signatureCanvas.height;
        const data = ctx.getImageData(0, 0, width, height).data;
        for (let i = 3; i < data.length; i += 4) {
            if (data[i] !== 0) {
                return false;
            }
        }
        return true;
    }

    async function getPhoto() {
        if (requestDraft.photoSignature.photoBlob) {
            return requestDraft.photoSignature.photoBlob;
        }

        if (!photoVideo || !photoCanvas || !photoVideo.videoWidth) {
            throw new Error("Camera indisponible.");
        }

        photoCanvas.width = photoVideo.videoWidth;
        photoCanvas.height = photoVideo.videoHeight;
        const ctx = photoCanvas.getContext("2d");
        if (!ctx) {
            throw new Error("Impossible de capturer la photo.");
        }

        ctx.drawImage(photoVideo, 0, 0, photoCanvas.width, photoCanvas.height);
        const blob = await new Promise((resolve, reject) => {
            photoCanvas.toBlob((nextBlob) => {
                if (!nextBlob) {
                    reject(new Error("Impossible de capturer la photo."));
                    return;
                }
                resolve(nextBlob);
            }, "image/jpeg", 0.92);
        });

        if (blob.size > 10 * 1024 * 1024) {
            throw new Error("Photo trop volumineuse.");
        }

        requestDraft.photoSignature.photoBlob = blob;
        showCapturedPhoto(photoCanvas);
        return blob;
    }

    async function getSignature() {
        if (requestDraft.photoSignature.signatureBlob) {
            return requestDraft.photoSignature.signatureBlob;
        }

        if (isSignatureBlank()) {
            throw new Error("Signature obligatoire.");
        }

        const blob = await new Promise((resolve, reject) => {
            signatureCanvas.toBlob((nextBlob) => {
                if (!nextBlob) {
                    reject(new Error("Impossible d'enregistrer la signature."));
                    return;
                }
                resolve(nextBlob);
            }, "image/png");
        });

        if (blob.size > 10 * 1024 * 1024) {
            throw new Error("Signature trop volumineuse.");
        }

        requestDraft.photoSignature.signatureBlob = blob;
        return blob;
    }

    function setupSignatureDrawing() {
        if (!signatureCanvas) {
            return;
        }

        resizeSignatureCanvas();
        signatureCanvas.style.touchAction = "none";

        let drawing = false;
        let lastX = 0;
        let lastY = 0;

        function getPoint(event) {
            const rect = signatureCanvas.getBoundingClientRect();
            if (event.touches && event.touches.length > 0) {
                return {
                    x: event.touches[0].clientX - rect.left,
                    y: event.touches[0].clientY - rect.top
                };
            }
            return {
                x: event.clientX - rect.left,
                y: event.clientY - rect.top
            };
        }

        function startDrawing(event) {
            if (signatureCanvas.width === 0 || signatureCanvas.height === 0) {
                resizeSignatureCanvas();
            }
            event.preventDefault();
            drawing = true;
            const point = getPoint(event);
            lastX = point.x;
            lastY = point.y;
            if (signatureCanvas.setPointerCapture) {
                signatureCanvas.setPointerCapture(event.pointerId);
            }
        }

        function draw(event) {
            if (!drawing) {
                return;
            }
            event.preventDefault();

            const ctx = signatureCanvas.getContext("2d");
            if (!ctx) {
                return;
            }

            const point = getPoint(event);
            const x = point.x;
            const y = point.y;

            ctx.beginPath();
            ctx.moveTo(lastX, lastY);
            ctx.lineTo(x, y);
            ctx.stroke();

            lastX = x;
            lastY = y;
            requestDraft.photoSignature.hasSignatureInk = true;
        }

        function stopDrawing(event) {
            drawing = false;
            if (event && signatureCanvas.releasePointerCapture) {
                signatureCanvas.releasePointerCapture(event.pointerId);
            }
        }

        signatureCanvas.addEventListener("pointerdown", startDrawing);
        signatureCanvas.addEventListener("pointermove", draw);
        signatureCanvas.addEventListener("pointerup", stopDrawing);
        signatureCanvas.addEventListener("pointerleave", stopDrawing);

        signatureCanvas.addEventListener("mousedown", startDrawing);
        signatureCanvas.addEventListener("mousemove", draw);
        signatureCanvas.addEventListener("mouseup", stopDrawing);
        signatureCanvas.addEventListener("mouseleave", stopDrawing);

        signatureCanvas.addEventListener("touchstart", startDrawing, { passive: false });
        signatureCanvas.addEventListener("touchmove", draw, { passive: false });
        signatureCanvas.addEventListener("touchend", stopDrawing, { passive: false });
    }

    function getFieldElement(fieldName) {
        return form.querySelector("[name='" + fieldName + "']");
    }

    function getFieldValue(fieldName) {
        const element = getFieldElement(fieldName);
        return element ? String(element.value || "").trim() : "";
    }

    function getFieldNumber(fieldName) {
        const value = getFieldValue(fieldName);
        return value ? Number(value) : null;
    }

    function getModelDefinition(modelName) {
        return formModels[modelName] || {};
    }

    function readSection(modelName) {
        const model = getModelDefinition(modelName);
        const values = {};

        Object.keys(model).forEach((fieldName) => {
            const fieldType = model[fieldName].type;
            const rawValue = getFieldValue(fieldName);
            values[fieldName] = fieldType === "number" ? (rawValue ? Number(rawValue) : null) : (rawValue || null);
        });

        return values;
    }

    function isSectionEmpty(values) {
        if (!values) {
            return true;
        }

        return Object.values(values).every((value) => value == null || value === "");
    }

    function writeSection(modelName, values) {
        const model = getModelDefinition(modelName);
        Object.keys(model).forEach((fieldName) => {
            const element = getFieldElement(fieldName);
            if (!element) {
                return;
            }

            const nextValue = values ? values[fieldName] : null;
            element.value = nextValue == null ? "" : String(nextValue);
        });
    }

    function getRequiredFields(modelName) {
        return Object.keys(getModelDefinition(modelName))
            .filter((fieldName) => Boolean(getModelDefinition(modelName)[fieldName].required))
            .map((fieldName) => ({
                name: fieldName,
                label: getModelDefinition(modelName)[fieldName].label || fieldName
            }));
    }

    function validateSection(modelName) {
        const requiredFields = getRequiredFields(modelName);
        for (const field of requiredFields) {
            if (!getFieldValue(field.name)) {
                return field.label + " est obligatoire.";
            }
        }
        return null;
    }

    function validateCurrentStep(step) {
        const stepContract = stepContracts[step] || {};
        if (step === 5) {
            return validatePiecesStep();
        }
        if (step === 6) {
            return validatePhotoSignatureStep();
        }

        if (step === 3 && isRequestTypeTransfer()) {
            const passportError = validateSection(stepContract.model);
            if (passportError) {
                return passportError;
            }
            return validateSection(stepContract.transferModel);
        }

        return stepContract.model ? validateSection(stepContract.model) : null;
    }

    function isRequestTypeDuplicata() {
        return getFieldNumber("idTypeDemande") === 2;
    }

    function isRequestTypeTransfer() {
        return getFieldNumber("idTypeDemande") === 3;
    }

    function isWithoutDataMode() {
        return Boolean(sansDonneesCheckbox && sansDonneesCheckbox.checked);
    }

    function syncTransferPassportNumbers() {
        if (!isRequestTypeTransfer()) {
            return;
        }

        const passportNumero = getFieldValue("numero");
        const newPassportNumero = getFieldValue("numeroNouveau");
        const passportNumeroField = getFieldElement("numero");
        const newPassportNumeroField = getFieldElement("numeroNouveau");

        if (passportNumero && !newPassportNumero && newPassportNumeroField) {
            newPassportNumeroField.value = passportNumero;
        }

        if (!passportNumero && newPassportNumero && passportNumeroField) {
            passportNumeroField.value = newPassportNumero;
        }
    }

    function syncWizardUi() {
        stepItems.forEach((stepItem) => {
            const stepNumber = Number(stepItem.dataset.step);
            stepItem.classList.toggle("is-active", stepNumber === activeStep);
            stepItem.classList.toggle("is-done", stepNumber < activeStep);
        });

        stepPanels.forEach((panel) => {
            panel.classList.toggle("is-active", Number(panel.dataset.stepPanel) === activeStep);
        });

        previousStepButton.style.display = activeStep === 1 ? "none" : "inline-block";
        nextStepButton.style.display = activeStep === stepMax ? "none" : "inline-block";
        submitButton.style.display = activeStep === stepMax ? "inline-block" : "none";

        if (activeStep === 6) {
            resizeSignatureCanvas();
        }
    }

    function syncRequestTypeUi() {
        const showWithoutDataToggle = isRequestTypeDuplicata() || isRequestTypeTransfer();
        if (sansDonneesBlock) {
            sansDonneesBlock.hidden = !showWithoutDataToggle;
        }

        if (!showWithoutDataToggle && sansDonneesCheckbox) {
            sansDonneesCheckbox.checked = false;
        }

        if (transferHelp) {
            transferHelp.hidden = !isRequestTypeTransfer();
        }

        if (newPassportHelp) {
            newPassportHelp.hidden = !isRequestTypeTransfer();
        }

        if (newPassportFields) {
            newPassportFields.hidden = !isRequestTypeTransfer();
        }
    }

    function syncTitle() {
        if (!titleEl) {
            return;
        }

        const typeSelect = getFieldElement("idTypeDemande");
        if (!typeSelect) {
            return;
        }

        const selectedOption = typeSelect.options[typeSelect.selectedIndex];
        const label = selectedOption && selectedOption.value ? selectedOption.textContent.trim() : "Nouveau titre";
        titleEl.textContent = editMode ? "modifier une demande" : "Demande de - " + label;
    }

    function loadImageInCanvas(url, canvas) {
        if (!url || !canvas) {
            return;
        }

        const img = new Image();
        img.onload = () => {
            canvas.width = img.naturalWidth;
            canvas.height = img.naturalHeight;
            const ctx = canvas.getContext("2d");
            if (ctx) {
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            }
            canvas.hidden = false;
        };
        img.src = url;
    }

    function hasExistingPhotoSignature() {
        return Boolean(requestDraft.photoSignature.existingPhoto && requestDraft.photoSignature.existingSignature);
    }

    function syncPhotoSignatureUi() {
        const hasExisting = hasExistingPhotoSignature();
        if (photoSignatureExistingEl) {
            photoSignatureExistingEl.hidden = !hasExisting;
        }

        [startCameraButton, capturePhotoButton, retakePhotoButton, clearSignatureButton].forEach((button) => {
            if (button) {
                button.disabled = hasExisting;
            }
        });

        if (signatureCanvas) {
            signatureCanvas.classList.toggle("is-disabled", hasExisting);
        }

        if (hasExisting) {
            if (photoVideo) {
                photoVideo.hidden = true;
            }
            if (photoCanvas) {
                loadImageInCanvas(requestDraft.photoSignature.existingPhoto, photoCanvas);
            }
            if (signatureCanvas) {
                loadImageInCanvas(requestDraft.photoSignature.existingSignature, signatureCanvas);
            }
        }
    }

    function getCategoryLabel(category) {
        return category === "COMMUNE" ? "Commune" : "Spécifique";
    }

    function getTypeLabel(isRequired) {
        return isRequired === true ? "Obligatoire" : "Facultatif";
    }

    function getFileNameFromUrl(url) {
        if (!url) {
            return "";
        }

        const parts = String(url).split("/");
        return parts.length > 0 ? parts[parts.length - 1] : String(url);
    }

    function fillSelect(fieldName, rows) {
        const select = getFieldElement(fieldName);
        if (!select) {
            return;
        }

        const emptyOption = select.querySelector("option[value='']");
        select.innerHTML = "";
        if (emptyOption) {
            select.appendChild(emptyOption);
        }

        rows.forEach((row) => {
            const option = document.createElement("option");
            option.value = String(row.id);
            option.textContent = row.libelle || row.nom || ("Valeur " + row.id);
            select.appendChild(option);
        });
    }

    function loadEditDetailIntoForm(detail) {
        if (!detail) {
            return;
        }

        requestDraft.editDetail = detail;
        requestDraft.requestIds.idDemandeur = detail.demandeur && detail.demandeur.idDemandeur ? detail.demandeur.idDemandeur : null;
        requestDraft.requestIds.idPassport = detail.passport && detail.passport.idPassport ? detail.passport.idPassport : null;
        requestDraft.requestIds.idVisaTransformable = detail.visaTransformable && detail.visaTransformable.idVisaTransformable
            ? detail.visaTransformable.idVisaTransformable
            : null;

        writeSection("requestSelection", {
            idTypeDemande: detail.idTypeDemande,
            idTypeVisa: detail.idTypeVisa,
            numeroDemande: detail.numero
        });
        writeSection("demandeurForm", detail.demandeur || {});
        writeSection("passportForm", detail.passport || {});
        writeSection("visaTransformableForm", detail.visaTransformable || {});

        if (detail.photoSignature) {
            requestDraft.photoSignature.existingPhoto = detail.photoSignature.lienPhoto || "";
            requestDraft.photoSignature.existingSignature = detail.photoSignature.lienSignature || "";
        }

        if (sansDonneesCheckbox) {
            sansDonneesCheckbox.checked = detail.idTypeDemande === 2 || detail.idTypeDemande === 3;
        }

        syncRequestTypeUi();
        syncTitle();
        syncPhotoSignatureUi();
    }

    async function loadEditDetail() {
        if (!editMode) {
            return null;
        }

        const detail = await getJson("/api/demandes/" + Number(editId));
        loadEditDetailIntoForm(detail);
        return detail;
    }

    async function loadReferenceData() {
        const rowsBySource = await Promise.all(referenceSources.map((source) => getJson(source.url)));
        referenceSources.forEach((source, index) => {
            fillSelect(source.fieldName, Array.isArray(rowsBySource[index]) ? rowsBySource[index] : []);
        });
    }

    async function loadPiecesCatalog() {
        const requestSelection = readSection("requestSelection");
        if (!requestSelection.idTypeDemande) {
            throw new Error("Type de demande requis pour charger les pieces.");
        }
        if (!requestSelection.idTypeVisa) {
            throw new Error("Type de visa requis pour charger les pieces.");
        }

        const rows = await getJson("/api/pieces-a-fournir?typeVisa=" + requestSelection.idTypeVisa
            + "&typeDemande=" + requestSelection.idTypeDemande);

        const existingPieces = new Map(
            ((requestDraft.editDetail && requestDraft.editDetail.piecesJointes) || []).map((piece) => [piece.idPieceAFournir, piece])
        );

        requestDraft.pieces = rows.map((piece) => {
            const existingPiece = existingPieces.get(piece.idPieceAFournir);
            return {
                idPieceAFournir: piece.idPieceAFournir,
                nom: piece.nom,
                categorie: piece.categorie,
                obligatoire: Boolean(piece.obligatoire),
                file: null,
                fileName: "",
                existingLien: existingPiece ? existingPiece.lien || "" : "",
                existingFileName: getFileNameFromUrl(existingPiece ? existingPiece.lien || "" : "")
            };
        });

        renderPiecesCatalog();
    }

    function renderPiecesCatalog() {
        piecesListEl.innerHTML = "";

        if (!requestDraft.pieces.length) {
            piecesListEl.textContent = "Aucune piece obligatoire trouvee pour cette combinaison.";
            return;
        }

        requestDraft.pieces.forEach((piece) => {
            const row = document.createElement("label");
            row.className = "piece-row";

            const info = document.createElement("div");
            info.className = "piece-row-info";
            info.textContent = piece.nom;

            const tag = document.createElement("span");
            tag.className = "piece-tag";
            tag.textContent = getCategoryLabel(piece.categorie) + " | " + getTypeLabel(piece.obligatoire);
            info.appendChild(tag);

            const uploadArea = document.createElement("div");
            uploadArea.className = "piece-upload";

            const input = document.createElement("input");
            input.type = "file";
            input.accept = ".pdf,.png,.jpg,.jpeg,.doc,.docx,.xls,.xlsx,.zip";
            input.addEventListener("change", () => {
                piece.file = input.files && input.files.length > 0 ? input.files[0] : null;
                piece.fileName = piece.file ? piece.file.name : "";
                fileLabel.textContent = piece.fileName || "Aucun fichier selectionne";
            });

            const fileLabel = document.createElement("span");
            fileLabel.className = "piece-filename";
            fileLabel.textContent = piece.existingFileName
                ? "Fichier actuel: " + piece.existingFileName
                : "Aucun fichier selectionne";

            if (piece.existingLien) {
                const existingLink = document.createElement("a");
                existingLink.className = "piece-link";
                existingLink.href = piece.existingLien;
                existingLink.target = "_blank";
                existingLink.rel = "noopener noreferrer";
                existingLink.textContent = "Ouvrir le fichier actuel";
                uploadArea.appendChild(existingLink);
            }

            row.appendChild(info);
            uploadArea.appendChild(input);
            uploadArea.appendChild(fileLabel);
            row.appendChild(uploadArea);
            piecesListEl.appendChild(row);
        });
    }

    function getUploadedPieces() {
        return requestDraft.pieces
            .filter((piece) => piece.file)
            .map((piece) => ({ idPieceAFournir: piece.idPieceAFournir }));
    }

    function getUploadedFiles() {
        return requestDraft.pieces
            .filter((piece) => piece.file)
            .map((piece) => piece.file);
    }

    function validatePiecesStep() {
        const missingRequiredPieces = requestDraft.pieces.filter((piece) => {
            const existingAttachmentIsEnough = editMode && Boolean(piece.existingLien);
            return piece.obligatoire && !piece.file && !existingAttachmentIsEnough;
        });
        if (missingRequiredPieces.length > 0) {
            return "Toutes les pieces obligatoires doivent avoir un fichier.";
        }
        return null;
    }

    function validatePhotoSignatureStep() {
        if (editMode && hasExistingPhotoSignature()
                && !requestDraft.photoSignature.photoBlob
                && !requestDraft.photoSignature.signatureBlob) {
            return null;
        }

        if (requestDraft.photoSignature.cameraError) {
            return requestDraft.photoSignature.cameraError;
        }

        if (!requestDraft.photoSignature.photoBlob) {
            return "La photo est obligatoire.";
        }

        if (isSignatureBlank()) {
            return "La signature est obligatoire.";
        }

        return null;
    }

    function buildRequestEnvelope() {
        const requestSelection = readSection("requestSelection");
        const visaTransformableValues = readSection("visaTransformableForm");
        return {
            numero: requestSelection.numeroDemande,
            idDemandeur: requestDraft.requestIds.idDemandeur,
            idPassport: requestDraft.requestIds.idPassport,
            idVisaTransformable: requestDraft.requestIds.idVisaTransformable,
            idTypeVisa: requestSelection.idTypeVisa,
            idTypeDemande: requestSelection.idTypeDemande,
            demandeur: readSection("demandeurForm"),
            passport: readSection("passportForm"),
            visaTransformable: isSectionEmpty(visaTransformableValues) ? null : visaTransformableValues
        };
    }

    function buildSourceRequestEnvelope() {
        const requestEnvelope = buildRequestEnvelope();
        return {
            ...requestEnvelope,
            idTypeDemande: 1,
            piecesJointes: getUploadedPieces()
        };
    }

    function buildTransferPassportPayload() {
        return readSection("passportNouveauForm");
    }

    async function createDemandeur() {
        const payload = readSection("demandeurForm");
        const response = await postJson("/api/demandeurs", payload);
        requestDraft.requestIds.idDemandeur = response.idDemandeur;
        return response;
    }

    async function createPassport() {
        const payload = readSection("passportForm");
        const response = await postJson("/api/demandeurs/" + requestDraft.requestIds.idDemandeur + "/passports", payload);
        requestDraft.requestIds.idPassport = response.idPassport;
        return response;
    }

    async function createTransferPassport() {
        const payload = buildTransferPassportPayload();
        if (!payload.numeroNouveau) {
            throw new Error("Veuillez renseigner le numero du nouveau passeport.");
        }

        const response = await postJson("/api/demandeurs/" + requestDraft.requestIds.idDemandeur + "/passports", {
            numero: payload.numeroNouveau,
            dateDelivrance: payload.dateDelivranceNouveau || null,
            dateExpiration: payload.dateExpirationNouveau || null
        });
        return response;
    }

    async function createVisaTransformable() {
        const payload = readSection("visaTransformableForm");
        const response = await postJson("/api/passports/" + requestDraft.requestIds.idPassport + "/visas-transformables", payload);
        requestDraft.requestIds.idVisaTransformable = response.id;
        return response;
    }

    function buildMultipartPayload(basePayload) {
        return postMultipart(basePayload.url, basePayload.body, basePayload.files, basePayload.method || "POST");
    }

    function shouldUploadPhotoSignature() {
        if (editMode && hasExistingPhotoSignature()
                && !requestDraft.photoSignature.photoBlob
                && !requestDraft.photoSignature.signatureBlob) {
            return false;
        }

        return true;
    }

    async function postPhotoSignature(idDemande) {
        const photoBlob = await getPhoto();
        const signatureBlob = await getSignature();

        const formData = new FormData();
        formData.append("photo", photoBlob, "photo.jpg");
        formData.append("signature", signatureBlob, "signature.png");

        const response = await fetch("/api/demandes/" + Number(idDemande) + "/photo-signature", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            throw await buildApiError(response);
        }

        return response.json();
    }

    async function submitNewWithData() {
        await createVisaTransformable();

        return buildMultipartPayload({
            url: "/api/demandes/nouveau-titre",
            body: {
                ...buildRequestEnvelope(),
                piecesJointes: getUploadedPieces()
            },
            files: getUploadedFiles()
        });
    }

    async function submitWithoutData() {
        const requestSelection = readSection("requestSelection");
        let newPassportResponse = null;
        let visaTransformableId = requestDraft.requestIds.idVisaTransformable;

        if (isRequestTypeTransfer()) {
            newPassportResponse = await createTransferPassport();
        }

        if (!visaTransformableId) {
            const visaPayload = readSection("visaTransformableForm");
            if (!visaPayload.referenceVisa || !visaPayload.dateEntreeMada) {
                throw new Error("Veuillez renseigner le visa transformable.");
            }
            const visaResponse = await createVisaTransformable();
            visaTransformableId = visaResponse.id;
        }

        const sourceRequest = {
            idDemandeur: requestDraft.requestIds.idDemandeur,
            idPassport: requestDraft.requestIds.idPassport,
            idVisaTransformable: visaTransformableId,
            idTypeVisa: requestSelection.idTypeVisa,
            idTypeDemande: 1,
            piecesJointes: getUploadedPieces()
        };

        if (isRequestTypeDuplicata()) {
            return buildMultipartPayload({
                url: "/api/demandes/duplicata/sans-donnees",
                body: {
                    demandeNouveauTitre: sourceRequest,
                    piecesCible: getUploadedPieces()
                },
                files: getUploadedFiles()
            });
        }

        if (isRequestTypeTransfer()) {
            return buildMultipartPayload({
                url: "/api/demandes/transfert/sans-donnees",
                body: {
                    demandeNouveauTitre: sourceRequest,
                    idPassportNouveau: newPassportResponse ? newPassportResponse.idPassport : null,
                    piecesCible: getUploadedPieces()
                },
                files: getUploadedFiles()
            });
        }

        throw new Error("Le type de demande selectionne n'est pas compatible avec ce mode.");
    }

    async function submitEditNouveauTitre(editIdentifier) {
        return buildMultipartPayload({
            url: "/api/demandes/nouveau-titre/" + Number(editIdentifier),
            body: {
                ...buildRequestEnvelope(),
                piecesJointes: getUploadedPieces()
            },
            files: getUploadedFiles(),
            method: "PUT"
        });
    }

    async function submitEditDuplicata(editIdentifier) {
        return buildMultipartPayload({
            url: "/api/demandes/duplicata/sans-donnees/" + Number(editIdentifier),
            body: {
                demandeNouveauTitre: {
                    ...buildSourceRequestEnvelope(),
                    piecesJointes: getUploadedPieces()
                },
                piecesCible: getUploadedPieces()
            },
            files: getUploadedFiles(),
            method: "PUT"
        });
    }

    async function submitEditTransfer(editIdentifier) {
        const transferPassport = buildTransferPassportPayload();
        if (!transferPassport.numeroNouveau) {
            throw new Error("Veuillez renseigner le numero du nouveau passeport.");
        }

        const passportResponse = await createTransferPassport();

        return buildMultipartPayload({
            url: "/api/demandes/transfert/sans-donnees/" + Number(editIdentifier),
            body: {
                demandeNouveauTitre: {
                    ...buildSourceRequestEnvelope(),
                    piecesJointes: getUploadedPieces()
                },
                idPassportNouveau: passportResponse.idPassport,
                piecesCible: getUploadedPieces()
            },
            files: getUploadedFiles(),
            method: "PUT"
        });
    }

    async function submitRequest() {
        setStatusMessage("Envoi en cours...");

        let response;
        let finalResponse;
        const requestSelection = readSection("requestSelection");

        syncTransferPassportNumbers();

        if (editMode) {
            if (!requestSelection.idTypeDemande) {
                throw new Error("Veuillez selectionner un type de demande.");
            }

            if (requestSelection.idTypeDemande === 1) {
                response = await submitEditNouveauTitre(editId);
            } else if (requestSelection.idTypeDemande === 2) {
                response = await submitEditDuplicata(editId);
            } else if (requestSelection.idTypeDemande === 3) {
                response = await submitEditTransfer(editId);
            } else {
                throw new Error("Le type de demande selectionne n'est pas compatible avec la modification.");
            }

            finalResponse = shouldUploadPhotoSignature() ? await postPhotoSignature(response.id) : response;

            window.location.href = confirmationUrl + "?" + new URLSearchParams({
                id: String(finalResponse.id || ""),
                numero: String(finalResponse.numero || ""),
                statut: String(finalResponse.statut || ""),
                date: String(finalResponse.dateDemande || "")
            }).toString();
            return;
        }

        await createDemandeur();
        await createPassport();

        if (isWithoutDataMode()) {
            response = await submitWithoutData();
        } else {
            if (requestSelection.idTypeDemande !== 1) {
                throw new Error("Veuillez activer 'sans donnees anterieures' pour ce type de demande.");
            }
            response = await submitNewWithData();
        }

        finalResponse = shouldUploadPhotoSignature() ? await postPhotoSignature(response.id) : response;

        window.location.href = confirmationUrl + "?" + new URLSearchParams({
            id: String(finalResponse.id || ""),
            numero: String(finalResponse.numero || ""),
            statut: String(finalResponse.statut || ""),
            date: String(finalResponse.dateDemande || "")
        }).toString();
    }

    async function postMultipart(url, body, files, method) {
        const formData = new FormData();
        formData.append("dto", new Blob([JSON.stringify(body)], { type: "application/json" }));

        (files || []).forEach((file) => {
            formData.append("files", file, file.name);
        });

        const response = await fetch(url, {
            method: method || "POST",
            body: formData
        });

        if (!response.ok) {
            throw await buildApiError(response);
        }

        return response.json();
    }

    async function getJson(url) {
        const response = await fetch(url, { method: "GET" });

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

    async function goToStep(stepNumber) {
        const targetStep = Number(stepNumber);
        if (Number.isNaN(targetStep) || targetStep < 1 || targetStep > stepMax) {
            return;
        }

        if (targetStep > activeStep) {
            for (let step = activeStep; step < targetStep; step += 1) {
                const error = validateCurrentStep(step);
                if (error) {
                    setStatusMessage(error);
                    return;
                }

                if (step === 1) {
                    requestDraft.requestSelection = readSection("requestSelection");
                }

                if (step === 4) {
                    try {
                        await loadPiecesCatalog();
                    } catch (error) {
                        setStatusMessage("Impossible de charger les pieces: " + (error.message || "erreur inconnue"));
                        return;
                    }
                }
            }
        }

        activeStep = targetStep;
        setStatusMessage("");
        syncWizardUi();
    }

    previousStepButton.addEventListener("click", () => {
        setStatusMessage("");
        activeStep = Math.max(1, activeStep - 1);
        syncWizardUi();
    });

    nextStepButton.addEventListener("click", async () => {
        const error = validateCurrentStep(activeStep);
        if (error) {
            setStatusMessage(error);
            return;
        }

        if (activeStep === 4) {
            try {
                await loadPiecesCatalog();
            } catch (exception) {
                setStatusMessage("Impossible de charger les pieces: " + (exception.message || "erreur inconnue"));
                return;
            }
        }

        setStatusMessage("");
        activeStep = Math.min(stepMax, activeStep + 1);
        syncWizardUi();
    });

    submitButton.addEventListener("click", async () => {
        const error = validateCurrentStep(stepMax);
        if (error) {
            setStatusMessage(error);
            return;
        }

        try {
            submitButton.disabled = true;
            requestDraft.requestSelection = readSection("requestSelection");
            await submitRequest();
        } catch (exception) {
            setStatusMessage("Echec de soumission: " + (exception.message || "erreur inconnue"));
        } finally {
            submitButton.disabled = false;
        }
    });

    stepItems.forEach((stepItem) => {
        const stepNumber = Number(stepItem.dataset.step);
        if (Number.isNaN(stepNumber)) {
            return;
        }

        stepItem.addEventListener("click", async () => {
            await goToStep(stepNumber);
        });

        stepItem.addEventListener("keydown", async (event) => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                await goToStep(stepNumber);
            }
        });
    });

    if (startCameraButton) {
        startCameraButton.addEventListener("click", async () => {
            await startCamera();
        });
    }

    if (capturePhotoButton) {
        capturePhotoButton.addEventListener("click", async () => {
            try {
                await getPhoto();
            } catch (error) {
                setPhotoStatus(error.message || "Impossible de capturer la photo.");
            }
        });
    }

    if (retakePhotoButton) {
        retakePhotoButton.addEventListener("click", async () => {
            resetPhotoCapture();
            await startCamera();
        });
    }

    if (clearSignatureButton) {
        clearSignatureButton.addEventListener("click", () => {
            clearSignatureCanvas();
        });
    }

    form.addEventListener("input", () => {
    });

    form.addEventListener("change", () => {
    });

    const requestTypeSelect = getFieldElement("idTypeDemande");
    if (requestTypeSelect) {
        requestTypeSelect.addEventListener("change", () => {
            requestDraft.requestSelection = readSection("requestSelection");
            syncRequestTypeUi();
            syncTitle();
        });
    }

    if (sansDonneesCheckbox) {
        sansDonneesCheckbox.addEventListener("change", () => {
            syncRequestTypeUi();
        });
    }

    async function initialize() {
        syncWizardUi();
        setupSignatureDrawing();
        syncPhotoSignatureUi();

        try {
            await loadReferenceData();

            if (editMode) {
                if (submitButton) {
                    submitButton.textContent = "Mettre a jour";
                }
                await loadEditDetail();
            }

            requestDraft.requestSelection = readSection("requestSelection");
            syncRequestTypeUi();
            syncTitle();

            if (activeStep === 5) {
                await loadPiecesCatalog();
            }
        } catch (error) {
            setStatusMessage("Impossible de charger les donnees de reference: " + (error.message || "erreur inconnue"));
        }
    }

    window.addEventListener("resize", () => {
        resizeSignatureCanvas();
    });

    window.addEventListener("beforeunload", () => {
        stopCamera();
    });

    initialize();
})();
