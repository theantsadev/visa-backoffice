package com.example.demo.metier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.metier.dto.DemandeDTO;
import com.example.demo.metier.dto.DemandeSoumiseDTO;
import com.example.demo.model.Demande;
import com.example.demo.model.DemandeDuplicataCarteResident;
import com.example.demo.model.DemandeNouveauTitre;
import com.example.demo.model.DemandeTransfertVisa;
import com.example.demo.model.Demandeur;
import com.example.demo.model.HistoriqueStatutDemande;
import com.example.demo.model.Passport;
import com.example.demo.model.PieceAFournir;
import com.example.demo.model.PieceJointe;
import com.example.demo.model.StatutDemande;
import com.example.demo.model.TypeDemande;
import com.example.demo.model.TypeVisa;
import com.example.demo.model.VisaTransformable;
import com.example.demo.repository.DemandeDuplicataCarteResidentRepository;
import com.example.demo.repository.DemandeNouveauTitreRepository;
import com.example.demo.repository.DemandeTransfertVisaRepository;
import com.example.demo.repository.DemandeRepository;
import com.example.demo.repository.DemandeurRepository;
import com.example.demo.repository.HistoriqueStatutDemandeRepository;
import com.example.demo.repository.PassportRepository;
import com.example.demo.repository.PieceAFournirRepository;
import com.example.demo.repository.PieceJointeRepository;
import com.example.demo.repository.StatutDemandeRepository;
import com.example.demo.repository.TypeDemandeRepository;
import com.example.demo.repository.TypeVisaRepository;
import com.example.demo.repository.VisaTransformableRepository;

@Service
public class DemandeEffectueeService {

    private static final Integer ID_STATUT_DOSSIER_CREE = 1;
    private static final Integer ID_STATUT_SCAN_TERMINE = 2;
    private static final Integer ID_STATUT_VISA_ACCORDE = 3;
    private static final Integer ID_TYPE_DEMANDE_NOUVEAU_TITRE = 1;
    private static final Integer ID_TYPE_DEMANDE_DUPLICATA = 2;
    private static final Integer ID_TYPE_DEMANDE_TRANSFERT = 3;

    private final DemandeurRepository demandeurRepository;
    private final PassportRepository passportRepository;
    private final VisaTransformableRepository visaTransformableRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final DemandeRepository demandeRepository;
    private final DemandeNouveauTitreRepository demandeNouveauTitreRepository;
    private final DemandeDuplicataCarteResidentRepository demandeDuplicataCarteResidentRepository;
    private final DemandeTransfertVisaRepository demandeTransfertVisaRepository;
    private final HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository;
    private final PieceAFournirRepository pieceAFournirRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final Path uploadRoot;

    public DemandeEffectueeService(
            DemandeurRepository demandeurRepository,
            PassportRepository passportRepository,
            VisaTransformableRepository visaTransformableRepository,
            TypeVisaRepository typeVisaRepository,
            TypeDemandeRepository typeDemandeRepository,
            StatutDemandeRepository statutDemandeRepository,
            DemandeRepository demandeRepository,
            DemandeNouveauTitreRepository demandeNouveauTitreRepository,
            DemandeDuplicataCarteResidentRepository demandeDuplicataCarteResidentRepository,
            DemandeTransfertVisaRepository demandeTransfertVisaRepository,
            HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository,
            PieceAFournirRepository pieceAFournirRepository,
            PieceJointeRepository pieceJointeRepository,
            @Value("${app.upload-dir:uploads/pieces-jointes}") String uploadDir) {
        this.demandeurRepository = demandeurRepository;
        this.passportRepository = passportRepository;
        this.visaTransformableRepository = visaTransformableRepository;
        this.typeVisaRepository = typeVisaRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.demandeRepository = demandeRepository;
        this.demandeNouveauTitreRepository = demandeNouveauTitreRepository;
        this.demandeDuplicataCarteResidentRepository = demandeDuplicataCarteResidentRepository;
        this.demandeTransfertVisaRepository = demandeTransfertVisaRepository;
        this.historiqueStatutDemandeRepository = historiqueStatutDemandeRepository;
        this.pieceAFournirRepository = pieceAFournirRepository;
        this.pieceJointeRepository = pieceJointeRepository;
        this.uploadRoot = Path.of(uploadDir);
    }

    @Transactional
    public DemandeSoumiseDTO soumettreDemande(DemandeDTO dto, List<MultipartFile> files) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }
        if (dto.getIdDemandeur() == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }
        if (dto.getIdPassport() == null) {
            throw new IllegalArgumentException("L'id du passeport est obligatoire.");
        }
        boolean sansDonneesAnterieures = dto.getIdVisaTransformable() == null;
        if (dto.getIdTypeVisa() == null) {
            throw new IllegalArgumentException("Le type de visa est obligatoire.");
        }
        if (dto.getIdTypeDemande() == null) {
            throw new IllegalArgumentException("Le type de demande est obligatoire.");
        }

        Demandeur demandeur = demandeurRepository.findById(dto.getIdDemandeur())
                .orElseThrow(
                        () -> new IllegalArgumentException("Demandeur introuvable pour id=" + dto.getIdDemandeur()));

        Passport passport = passportRepository.findById(dto.getIdPassport())
                .orElseThrow(
                        () -> new IllegalArgumentException("Passeport introuvable pour id=" + dto.getIdPassport()));

        if (passport.getDemandeur() == null || !dto.getIdDemandeur().equals(passport.getDemandeur().getIdDemandeur())) {
            throw new IllegalArgumentException("Le passeport selectionne n'appartient pas au demandeur.");
        }

        VisaTransformable visaTransformable = null;
        if (!sansDonneesAnterieures) {
            visaTransformable = visaTransformableRepository.findById(dto.getIdVisaTransformable())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Visa transformable introuvable pour id=" + dto.getIdVisaTransformable()));

            if (visaTransformable.getDemandeur() == null
                    || !dto.getIdDemandeur().equals(visaTransformable.getDemandeur().getIdDemandeur())) {
                throw new IllegalArgumentException("Le visa transformable selectionne n'appartient pas au demandeur.");
            }

            if (visaTransformable.getPassport() == null
                    || !dto.getIdPassport().equals(visaTransformable.getPassport().getIdPassport())) {
                throw new IllegalArgumentException("Le visa transformable selectionne n'appartient pas au passeport.");
            }
        }

        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(
                        () -> new IllegalArgumentException("Type de visa introuvable pour id=" + dto.getIdTypeVisa()));

        if (!ID_TYPE_DEMANDE_NOUVEAU_TITRE.equals(dto.getIdTypeDemande())) {
            throw new IllegalArgumentException("Le type de demande doit etre 'Nouveau titre'.");
        }

        StatutDemande statut = determinerStatut(dto.getPiecesJointes(), dto.getIdTypeVisa(), dto.getIdTypeDemande());

        Demande demandeCreee = creerDemandeEtNouveauTitre(
                demandeur,
                typeVisa,
                visaTransformable,
                passport,
                ID_TYPE_DEMANDE_NOUVEAU_TITRE,
                statut.getId());

        savePiecesJointes(dto.getPiecesJointes(), files, demandeCreee.getId());

        return new DemandeSoumiseDTO(demandeCreee.getId(), statut.getLibelle(), demandeCreee.getDateDemande());
    }

    @Transactional
    public DemandeSoumiseDTO soumettreDuplicataSansDonnees(DemandeDTO dtoNouveauTitre,
            List<DemandeDTO.PieceJointeDTO> piecesCible,
            List<MultipartFile> files) {
        if (dtoNouveauTitre == null) {
            throw new IllegalArgumentException("La demande nouveau titre est obligatoire.");
        }

        Demande demandeSource = creerDemandeEtNouveauTitreSansDonnees(dtoNouveauTitre);

        StatutDemande statut = determinerStatut(piecesCible, dtoNouveauTitre.getIdTypeVisa(), ID_TYPE_DEMANDE_DUPLICATA);

        Demande demandeCible = creerDemandeCible(dtoNouveauTitre.getIdDemandeur(), ID_TYPE_DEMANDE_DUPLICATA,
                statut.getId());

        DemandeDuplicataCarteResident duplicata = new DemandeDuplicataCarteResident();
        duplicata.setDemande(demandeCible);
        duplicata.setDemandeNouveauTitreSource(getDemandeNouveauTitre(demandeSource));
        demandeDuplicataCarteResidentRepository.save(duplicata);

        savePiecesJointes(piecesCible, files, demandeCible.getId());

        return new DemandeSoumiseDTO(demandeCible.getId(), statut.getLibelle(), demandeCible.getDateDemande());
    }

    @Transactional
    public DemandeSoumiseDTO soumettreTransfertSansDonnees(DemandeDTO dtoNouveauTitre,
            Integer idPassportNouveau,
            List<DemandeDTO.PieceJointeDTO> piecesCible,
            List<MultipartFile> files) {
        if (dtoNouveauTitre == null) {
            throw new IllegalArgumentException("La demande nouveau titre est obligatoire.");
        }
        if (idPassportNouveau == null) {
            throw new IllegalArgumentException("Le nouveau passeport est obligatoire.");
        }

        Demande demandeSource = creerDemandeEtNouveauTitreSansDonnees(dtoNouveauTitre);

        StatutDemande statut = determinerStatut(piecesCible, dtoNouveauTitre.getIdTypeVisa(), ID_TYPE_DEMANDE_TRANSFERT);

        Demande demandeCible = creerDemandeCible(dtoNouveauTitre.getIdDemandeur(), ID_TYPE_DEMANDE_TRANSFERT,
                statut.getId());

        Passport passportNouveau = passportRepository.findById(idPassportNouveau)
                .orElseThrow(() -> new IllegalArgumentException("Passeport introuvable pour id=" + idPassportNouveau));

        if (passportNouveau.getDemandeur() == null
                || !dtoNouveauTitre.getIdDemandeur().equals(passportNouveau.getDemandeur().getIdDemandeur())) {
            throw new IllegalArgumentException("Le passeport selectionne n'appartient pas au demandeur.");
        }

        DemandeTransfertVisa transfert = new DemandeTransfertVisa();
        transfert.setDemande(demandeCible);
        transfert.setPassport(passportNouveau);
        transfert.setDemandeNouveauTitreSource(getDemandeNouveauTitre(demandeSource));
        demandeTransfertVisaRepository.save(transfert);

        savePiecesJointes(piecesCible, files, demandeCible.getId());

        return new DemandeSoumiseDTO(demandeCible.getId(), statut.getLibelle(), demandeCible.getDateDemande());
    }

        public void validerPiecesObligatoires(List<DemandeDTO.PieceJointeDTO> pieces, Integer idTypeVisa,
            Integer idTypeDemande) {
        if (idTypeVisa == null) {
            throw new IllegalArgumentException("L'id type visa est obligatoire pour valider les pieces.");
        }
        if (idTypeDemande == null) {
            throw new IllegalArgumentException("L'id type demande est obligatoire pour valider les pieces.");
        }

        List<DemandeDTO.PieceJointeDTO> safePieces = pieces == null ? Collections.emptyList() : pieces;

        Set<Integer> piecesFournies = safePieces.stream()
                .map(DemandeDTO.PieceJointeDTO::getIdPieceAFournir)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        List<PieceAFournir> piecesObligatoires = pieceAFournirRepository.findPiecesObligatoires(
            idTypeVisa, idTypeDemande);

        for (PieceAFournir pieceObligatoire : piecesObligatoires) {
            if (!piecesFournies.contains(pieceObligatoire.getId())) {
                throw new IllegalArgumentException(
                        "Piece obligatoire manquante (id=" + pieceObligatoire.getId() + ").");
            }
        }
    }

    private StatutDemande determinerStatut(List<DemandeDTO.PieceJointeDTO> pieces, Integer idTypeVisa,
            Integer idTypeDemande) {
        validerPiecesObligatoires(pieces, idTypeVisa, idTypeDemande);

        Set<Integer> piecesFournies = (pieces == null ? Collections.<DemandeDTO.PieceJointeDTO>emptyList() : pieces)
                .stream()
                .map(DemandeDTO.PieceJointeDTO::getIdPieceAFournir)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        List<PieceAFournir> piecesCommunes = pieceAFournirRepository.findByTypeVisaIsNullAndTypeDemandeIsNull();
        List<PieceAFournir> piecesParVisa = pieceAFournirRepository.findByTypeVisa_IdTypeVisa(idTypeVisa);
        List<PieceAFournir> piecesParDemande = pieceAFournirRepository.findByTypeDemande_IdTypeDemande(idTypeDemande);

        Set<Integer> toutesLesPieces = new java.util.LinkedHashSet<>();
        piecesCommunes.forEach(piece -> toutesLesPieces.add(piece.getId()));
        piecesParVisa.forEach(piece -> toutesLesPieces.add(piece.getId()));
        piecesParDemande.forEach(piece -> toutesLesPieces.add(piece.getId()));

        Integer idStatut = piecesFournies.containsAll(toutesLesPieces)
                ? ID_STATUT_SCAN_TERMINE
                : ID_STATUT_DOSSIER_CREE;

        return statutDemandeRepository.findById(idStatut)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable pour id=" + idStatut));
    }

    private Demande creerDemandeEtNouveauTitreSansDonnees(DemandeDTO dto) {
        if (dto.getIdDemandeur() == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }
        if (dto.getIdPassport() == null) {
            throw new IllegalArgumentException("L'id du passeport est obligatoire.");
        }
        if (dto.getIdVisaTransformable() == null) {
            throw new IllegalArgumentException("L'id du visa transformable est obligatoire.");
        }
        if (dto.getIdTypeVisa() == null) {
            throw new IllegalArgumentException("Le type de visa est obligatoire.");
        }

        Demandeur demandeur = demandeurRepository.findById(dto.getIdDemandeur())
                .orElseThrow(
                        () -> new IllegalArgumentException("Demandeur introuvable pour id=" + dto.getIdDemandeur()));

        Passport passport = passportRepository.findById(dto.getIdPassport())
                .orElseThrow(
                        () -> new IllegalArgumentException("Passeport introuvable pour id=" + dto.getIdPassport()));

        if (passport.getDemandeur() == null || !dto.getIdDemandeur().equals(passport.getDemandeur().getIdDemandeur())) {
            throw new IllegalArgumentException("Le passeport selectionne n'appartient pas au demandeur.");
        }

        VisaTransformable visaTransformable = visaTransformableRepository.findById(dto.getIdVisaTransformable())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Visa transformable introuvable pour id=" + dto.getIdVisaTransformable()));

        if (visaTransformable.getDemandeur() == null
                || !dto.getIdDemandeur().equals(visaTransformable.getDemandeur().getIdDemandeur())) {
            throw new IllegalArgumentException("Le visa transformable selectionne n'appartient pas au demandeur.");
        }

        if (visaTransformable.getPassport() == null
                || !dto.getIdPassport().equals(visaTransformable.getPassport().getIdPassport())) {
            throw new IllegalArgumentException("Le visa transformable selectionne n'appartient pas au passeport.");
        }

        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(
                        () -> new IllegalArgumentException("Type de visa introuvable pour id=" + dto.getIdTypeVisa()));

        return creerDemandeEtNouveauTitre(demandeur, typeVisa, visaTransformable, passport, ID_TYPE_DEMANDE_NOUVEAU_TITRE,
                ID_STATUT_VISA_ACCORDE);
    }

    private Demande creerDemandeEtNouveauTitre(Demandeur demandeur,
            TypeVisa typeVisa,
            VisaTransformable visaTransformable,
            Passport passport,
            Integer idTypeDemande,
            Integer idStatut) {
        TypeDemande typeDemande = typeDemandeRepository.findById(idTypeDemande)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de demande introuvable pour id=" + idTypeDemande));

        Demande demande = new Demande();
        demande.setDateDemande(LocalDateTime.now());
        demande.setDemandeur(demandeur);
        demande.setTypeDemande(typeDemande);

        Demande demandeCreee = demandeRepository.save(demande);

        DemandeNouveauTitre demandeNouveauTitre = new DemandeNouveauTitre();
        demandeNouveauTitre.setDemande(demandeCreee);
        demandeNouveauTitre.setPassport(passport);
        demandeNouveauTitre.setVisaTransformable(visaTransformable);
        demandeNouveauTitre.setTypeVisa(typeVisa);
        demandeNouveauTitreRepository.save(demandeNouveauTitre);

        enregistrerStatut(demandeCreee.getId(), idStatut);

        return demandeCreee;
    }

    private Demande creerDemandeCible(Integer idDemandeur, Integer idTypeDemande, Integer idStatut) {
        if (idDemandeur == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }

        Demandeur demandeur = demandeurRepository.findById(idDemandeur)
                .orElseThrow(() -> new IllegalArgumentException("Demandeur introuvable pour id=" + idDemandeur));

        TypeDemande typeDemande = typeDemandeRepository.findById(idTypeDemande)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de demande introuvable pour id=" + idTypeDemande));

        Demande demande = new Demande();
        demande.setDateDemande(LocalDateTime.now());
        demande.setDemandeur(demandeur);
        demande.setTypeDemande(typeDemande);

        Demande demandeCreee = demandeRepository.save(demande);
        enregistrerStatut(demandeCreee.getId(), idStatut);
        return demandeCreee;
    }

    private void enregistrerStatut(Integer idDemande, Integer idStatut) {
        StatutDemande statut = statutDemandeRepository.findById(idStatut)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable pour id=" + idStatut));

        HistoriqueStatutDemande historique = new HistoriqueStatutDemande();
        historique.setIdDemande(idDemande);
        historique.setStatutDemande(statut.getId());
        historique.setDateHeureHistorique(LocalDateTime.now());
        historiqueStatutDemandeRepository.save(historique);
    }

    private DemandeNouveauTitre getDemandeNouveauTitre(Demande demande) {
        if (demande == null) {
            return null;
        }

        if (demande.getDemandeNouveauTitre() != null) {
            return demande.getDemandeNouveauTitre();
        }

        return demandeNouveauTitreRepository.findById(demande.getId()).orElse(null);
    }

    public void savePiecesJointes(List<DemandeDTO.PieceJointeDTO> pieces,
            List<MultipartFile> files,
            Integer idDemande) {
        if (idDemande == null) {
            throw new IllegalArgumentException("L'id de la demande est obligatoire.");
        }

        List<DemandeDTO.PieceJointeDTO> safePieces = pieces == null ? Collections.emptyList() : pieces;
        if (safePieces.isEmpty()) {
            return;
        }

        List<MultipartFile> safeFiles = files == null ? Collections.emptyList() : files;
        if (safePieces.size() != safeFiles.size()) {
            throw new IllegalArgumentException("Chaque piece jointe doit avoir un fichier associe.");
        }

        for (int index = 0; index < safePieces.size(); index++) {
            DemandeDTO.PieceJointeDTO piece = safePieces.get(index);
            MultipartFile file = safeFiles.get(index);
            if (piece == null || piece.getIdPieceAFournir() == null) {
                throw new IllegalArgumentException("Chaque piece jointe doit avoir un identifiant valide.");
            }
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier de la piece jointe est obligatoire.");
            }

            String lien = enregistrerFichierPieceJointe(idDemande, piece.getIdPieceAFournir(), file);
            PieceJointe pieceJointe = new PieceJointe();
            pieceJointe.setIdPieceAFournir(piece.getIdPieceAFournir());
            pieceJointe.setIdDemande(idDemande);
            pieceJointe.setLien(lien);
            pieceJointeRepository.save(pieceJointe);
        }
    }

    private String enregistrerFichierPieceJointe(Integer idDemande, Integer idPieceAFournir, MultipartFile file) {
        try {
            Path dossierDemande = uploadRoot.resolve(String.valueOf(idDemande));
            Files.createDirectories(dossierDemande);

            String nomOriginal = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String nomBase = Paths.get(nomOriginal).getFileName().toString();
            if (nomBase.isBlank()) {
                nomBase = "piece";
            }

            String nomSecurise = nomBase.replaceAll("[^a-zA-Z0-9._-]", "_");
            String nomStocke = idPieceAFournir + "-" + UUID.randomUUID() + "-" + nomSecurise;
            Path destination = dossierDemande.resolve(nomStocke);

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + idDemande + "/" + nomStocke;
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier de piece jointe.", e);
        }
    }
}
