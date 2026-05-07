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
import com.example.demo.metier.dto.DemandeurDTO;
import com.example.demo.metier.dto.PassportDTO;
import com.example.demo.metier.dto.VisaTransformableDTO;
import com.example.demo.metier.dto.DemandeSoumiseDTO;
import com.example.demo.model.Demande;
import com.example.demo.model.DemandeDuplicataCarteResident;
import com.example.demo.model.DemandeNouveauTitre;
import com.example.demo.model.DemandeTransfertVisa;
import com.example.demo.model.Demandeur;
import com.example.demo.model.HistoriqueStatutDemande;
import com.example.demo.model.Nationnalite;
import com.example.demo.model.Passport;
import com.example.demo.model.PieceAFournir;
import com.example.demo.model.PieceJointe;
import com.example.demo.model.SituationFamiliale;
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
import com.example.demo.repository.NationnaliteRepository;
import com.example.demo.repository.PassportRepository;
import com.example.demo.repository.PieceAFournirRepository;
import com.example.demo.repository.PieceJointeRepository;
import com.example.demo.repository.SituationFamilialeRepository;
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
    private final NationnaliteRepository nationnaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
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
            NationnaliteRepository nationnaliteRepository,
            SituationFamilialeRepository situationFamilialeRepository,
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
        this.nationnaliteRepository = nationnaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.uploadRoot = Path.of(uploadDir);
    }

    @Transactional
    public DemandeSoumiseDTO soumettreDemande(DemandeDTO dto, List<MultipartFile> files) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }
        String numeroDemande = obtenirNumeroDemande(dto.getNumero());

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

        Demande demandeCreee = creerDemandeEtNouveauTitre(
                demandeur,
                typeVisa,
                visaTransformable,
                passport,
            numeroDemande,
                ID_TYPE_DEMANDE_NOUVEAU_TITRE,
                ID_STATUT_DOSSIER_CREE);

        StatutDemande statut = determinerStatut(demandeCreee, dto.getPiecesJointes());
        enregistrerStatutSiNecessaire(demandeCreee.getId(), statut.getId());

        savePiecesJointes(dto.getPiecesJointes(), files, demandeCreee.getId());

        return new DemandeSoumiseDTO(demandeCreee.getId(), demandeCreee.getNumero(), statut.getLibelle(),
            demandeCreee.getDateDemande());
    }

    @Transactional
    public DemandeSoumiseDTO mettreAJourDemandeNouveauTitre(Integer idDemande, DemandeDTO dto,
            List<MultipartFile> files) {
        Demande demande = chargerDemandeEditable(idDemande, ID_TYPE_DEMANDE_NOUVEAU_TITRE);
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }

        if (!isBlank(dto.getNumero()) && !dto.getNumero().trim().equals(demande.getNumero())) {
            throw new IllegalArgumentException("Le numero de la demande ne peut pas etre modifie.");
        }

        Demandeur demandeur = chargerDemandeur(dto.getIdDemandeur());
        Passport passport = chargerPassport(dto.getIdPassport(), dto.getIdDemandeur());
        VisaTransformable visaTransformable = chargerVisaTransformable(dto.getIdVisaTransformable(),
                dto.getIdDemandeur(),
                dto.getIdPassport());
        TypeVisa typeVisa = chargerTypeVisa(dto.getIdTypeVisa());

        demandeur = mettreAJourDemandeur(demandeur, dto.getDemandeur());
        passport = mettreAJourPassport(passport, dto.getPassport());
        visaTransformable = mettreAJourVisaTransformable(visaTransformable, dto.getVisaTransformable());

        demande.setDemandeur(demandeur);
        demandeRepository.save(demande);

        DemandeNouveauTitre demandeNouveauTitre = getDemandeNouveauTitre(demande);
        if (demandeNouveauTitre == null) {
            demandeNouveauTitre = new DemandeNouveauTitre();
            demandeNouveauTitre.setDemande(demande);
        }
        demandeNouveauTitre.setPassport(passport);
        demandeNouveauTitre.setVisaTransformable(visaTransformable);
        demandeNouveauTitre.setTypeVisa(typeVisa);
        demandeNouveauTitreRepository.save(demandeNouveauTitre);

        remplacerPiecesJointes(demande.getId(), dto.getPiecesJointes(), files);
        StatutDemande statut = determinerStatut(demande, dto.getPiecesJointes());
        enregistrerStatutSiNecessaire(demande.getId(), statut.getId());
        return new DemandeSoumiseDTO(demande.getId(), demande.getNumero(), statut.getLibelle(), demande.getDateDemande());
    }

    @Transactional
    public DemandeSoumiseDTO soumettreDuplicataSansDonnees(DemandeDTO dtoNouveauTitre,
            List<DemandeDTO.PieceJointeDTO> piecesCible,
            List<MultipartFile> files) {
        if (dtoNouveauTitre == null) {
            throw new IllegalArgumentException("La demande nouveau titre est obligatoire.");
        }

        Demande demandeSource = creerDemandeEtNouveauTitreSansDonnees(dtoNouveauTitre);

        Demande demandeCible = creerDemandeCible(dtoNouveauTitre.getIdDemandeur(), ID_TYPE_DEMANDE_DUPLICATA,
                ID_STATUT_DOSSIER_CREE);

        DemandeDuplicataCarteResident duplicata = new DemandeDuplicataCarteResident();
        duplicata.setDemande(demandeCible);
        duplicata.setDemandeNouveauTitreSource(getDemandeNouveauTitre(demandeSource));
        demandeDuplicataCarteResidentRepository.save(duplicata);
        demandeCible.setDemandeDuplicataCarteResident(duplicata);

        savePiecesJointes(piecesCible, files, demandeCible.getId());

        StatutDemande statut = determinerStatut(demandeCible, piecesCible);
        enregistrerStatutSiNecessaire(demandeCible.getId(), statut.getId());

        return new DemandeSoumiseDTO(demandeCible.getId(), demandeCible.getNumero(), statut.getLibelle(),
            demandeCible.getDateDemande());
    }

    public Demande getDemandeById(Integer id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable pour id=" + id));
    }

    @Transactional
    public DemandeSoumiseDTO mettreAJourDuplicataSansDonnees(Integer idDemande,
            DemandeDTO dtoNouveauTitre,
            List<DemandeDTO.PieceJointeDTO> piecesCible,
            List<MultipartFile> files) {
        Demande demandeCible = chargerDemandeEditable(idDemande, ID_TYPE_DEMANDE_DUPLICATA);
        if (dtoNouveauTitre == null) {
            throw new IllegalArgumentException("La demande nouveau titre est obligatoire.");
        }

        DemandeDuplicataCarteResident duplicata = demandeCible.getDemandeDuplicataCarteResident();
        if (duplicata == null) {
            duplicata = demandeDuplicataCarteResidentRepository.findById(idDemande)
                    .orElseThrow(() -> new IllegalArgumentException("Duplicata introuvable pour id=" + idDemande));
        }

        DemandeNouveauTitre source = chargerSourceNouveauTitre(demandeCible);
        mettreAJourDemandeNouveauTitreSource(source, dtoNouveauTitre);

        demandeDuplicataCarteResidentRepository.save(duplicata);
        demandeRepository.save(demandeCible);
        demandeCible.setDemandeDuplicataCarteResident(duplicata);

        remplacerPiecesJointes(demandeCible.getId(), piecesCible, files);
        StatutDemande statut = determinerStatut(demandeCible, piecesCible);
        enregistrerStatutSiNecessaire(demandeCible.getId(), statut.getId());

        return new DemandeSoumiseDTO(demandeCible.getId(), demandeCible.getNumero(), statut.getLibelle(),
            demandeCible.getDateDemande());
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

        Demande demandeCible = creerDemandeCible(dtoNouveauTitre.getIdDemandeur(), ID_TYPE_DEMANDE_TRANSFERT,
                ID_STATUT_DOSSIER_CREE);

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
        demandeCible.setDemandeTransfertVisa(transfert);

        savePiecesJointes(piecesCible, files, demandeCible.getId());

        StatutDemande statut = determinerStatut(demandeCible, piecesCible);
        enregistrerStatutSiNecessaire(demandeCible.getId(), statut.getId());

        return new DemandeSoumiseDTO(demandeCible.getId(), statut.getLibelle(), demandeCible.getDateDemande());
    }

    @Transactional
    public DemandeSoumiseDTO mettreAJourTransfertSansDonnees(Integer idDemande,
            DemandeDTO dtoNouveauTitre,
            Integer idPassportNouveau,
            List<DemandeDTO.PieceJointeDTO> piecesCible,
            List<MultipartFile> files) {
        Demande demandeCible = chargerDemandeEditable(idDemande, ID_TYPE_DEMANDE_TRANSFERT);
        if (dtoNouveauTitre == null) {
            throw new IllegalArgumentException("La demande nouveau titre est obligatoire.");
        }
        if (idPassportNouveau == null) {
            throw new IllegalArgumentException("Le nouveau passeport est obligatoire.");
        }

        DemandeTransfertVisa transfert = demandeCible.getDemandeTransfertVisa();
        if (transfert == null) {
            transfert = demandeTransfertVisaRepository.findById(idDemande)
                    .orElseThrow(() -> new IllegalArgumentException("Transfert introuvable pour id=" + idDemande));
        }

        DemandeNouveauTitre source = chargerSourceNouveauTitre(demandeCible);
        mettreAJourDemandeNouveauTitreSource(source, dtoNouveauTitre);

        Passport passportNouveau = chargerPassport(idPassportNouveau, dtoNouveauTitre.getIdDemandeur());
        transfert.setPassport(passportNouveau);
        demandeTransfertVisaRepository.save(transfert);
        demandeRepository.save(demandeCible);
        demandeCible.setDemandeTransfertVisa(transfert);

        remplacerPiecesJointes(demandeCible.getId(), piecesCible, files);
        StatutDemande statut = determinerStatut(demandeCible, piecesCible);
        enregistrerStatutSiNecessaire(demandeCible.getId(), statut.getId());

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

    private StatutDemande determinerStatut(Demande demande, List<DemandeDTO.PieceJointeDTO> pieces) {
        if (demande == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }

        DemandeNouveauTitre demandeNouveauTitre = chargerSourceNouveauTitre(demande);

        if (demandeNouveauTitre == null || demandeNouveauTitre.getTypeVisa() == null
                || demande.getTypeDemande() == null || demande.getTypeDemande().getId() == null) {
            throw new IllegalArgumentException("Les informations de la demande sont incomplètes.");
        }

        Integer idTypeVisa = demandeNouveauTitre.getTypeVisa().getId();
        Integer idTypeDemande = demande.getTypeDemande().getId();
        // validerPiecesObligatoires(pieces, idTypeVisa, idTypeDemande);

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

        boolean toutesLesPiecesCompletes = piecesFournies.containsAll(toutesLesPieces);
        boolean tousLesChampsOptionnelsComplets = sontChampsOptionnelsComplets(demandeNouveauTitre);

        Integer idStatut = (toutesLesPiecesCompletes && tousLesChampsOptionnelsComplets)
                ? ID_STATUT_SCAN_TERMINE
                : ID_STATUT_DOSSIER_CREE;

        return statutDemandeRepository.findById(idStatut)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable pour id=" + idStatut));
    }

    @Transactional
    public DemandeSoumiseDTO validerParAdmin(Integer idDemande, String statutLibelle) {
        if (idDemande == null) {
            throw new IllegalArgumentException("L'id de la demande est obligatoire.");
        }
        if (isBlank(statutLibelle)) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable pour id=" + idDemande));

        StatutDemande statut = statutDemandeRepository.findByLibelleIgnoreCase(statutLibelle)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + statutLibelle));

        enregistrerStatutSiNecessaire(demande.getId(), statut.getId());
        return new DemandeSoumiseDTO(demande.getId(), statut.getLibelle(), demande.getDateDemande());
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

        String numeroDemande = obtenirNumeroDemande(dto.getNumero());

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

        return creerDemandeEtNouveauTitre(demandeur, typeVisa, visaTransformable, passport, numeroDemande,
                ID_TYPE_DEMANDE_NOUVEAU_TITRE,
                ID_STATUT_VISA_ACCORDE);
    }

    private Demande creerDemandeEtNouveauTitre(Demandeur demandeur,
            TypeVisa typeVisa,
            VisaTransformable visaTransformable,
            Passport passport,
            String numeroDemande,
            Integer idTypeDemande,
            Integer idStatut) {
        TypeDemande typeDemande = typeDemandeRepository.findById(idTypeDemande)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de demande introuvable pour id=" + idTypeDemande));

        Demande demande = new Demande();
        demande.setNumero(numeroDemande);
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
        demande.setNumero(genererNumeroDemande());
        demande.setDateDemande(LocalDateTime.now());
        demande.setDemandeur(demandeur);
        demande.setTypeDemande(typeDemande);

        Demande demandeCreee = demandeRepository.save(demande);
        enregistrerStatut(demandeCreee.getId(), idStatut);
        return demandeCreee;
    }

    private Demande chargerDemandeEditable(Integer idDemande, Integer idTypeDemandeAttendu) {
        if (idDemande == null) {
            throw new IllegalArgumentException("L'id de la demande est obligatoire.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable pour id=" + idDemande));

        if (demande.getTypeDemande() == null || demande.getTypeDemande().getId() == null
                || !idTypeDemandeAttendu.equals(demande.getTypeDemande().getId())) {
            throw new IllegalArgumentException("Le type de demande ne correspond pas.");
        }

        StatutDemande statutActuel = getStatutActuel(demande.getId());
        if (statutActuel != null && !ID_STATUT_DOSSIER_CREE.equals(statutActuel.getId())) {
            throw new IllegalArgumentException("La demande n'est plus modifiable.");
        }

        return demande;
    }

    private StatutDemande getStatutActuel(Integer idDemande) {
        if (idDemande == null) {
            return null;
        }

        return historiqueStatutDemandeRepository.findTopByIdDemandeOrderByDateHeureHistoriqueDesc(idDemande)
                .map(historique -> historique.getStatutDemande())
                .flatMap(statutDemandeRepository::findById)
                .orElse(null);
    }

    private void enregistrerStatutSiNecessaire(Integer idDemande, Integer idStatut) {
        if (idDemande == null || idStatut == null) {
            return;
        }

        StatutDemande statutActuel = getStatutActuel(idDemande);
        if (statutActuel != null && idStatut.equals(statutActuel.getId())) {
            return;
        }

        enregistrerStatut(idDemande, idStatut);
    }

    private Demandeur chargerDemandeur(Integer idDemandeur) {
        if (idDemandeur == null) {
            throw new IllegalArgumentException("L'id du demandeur est obligatoire.");
        }

        return demandeurRepository.findById(idDemandeur)
                .orElseThrow(() -> new IllegalArgumentException("Demandeur introuvable pour id=" + idDemandeur));
    }

    private Passport chargerPassport(Integer idPassport, Integer idDemandeur) {
        if (idPassport == null) {
            throw new IllegalArgumentException("L'id du passeport est obligatoire.");
        }

        Passport passport = passportRepository.findById(idPassport)
                .orElseThrow(() -> new IllegalArgumentException("Passeport introuvable pour id=" + idPassport));

        if (passport.getDemandeur() == null || idDemandeur == null
                || !idDemandeur.equals(passport.getDemandeur().getIdDemandeur())) {
            throw new IllegalArgumentException("Le passeport selectionne n'appartient pas au demandeur.");
        }

        return passport;
    }

    private VisaTransformable chargerVisaTransformable(Integer idVisaTransformable, Integer idDemandeur,
            Integer idPassport) {
        if (idVisaTransformable == null) {
            return null;
        }

        VisaTransformable visaTransformable = visaTransformableRepository.findById(idVisaTransformable)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Visa transformable introuvable pour id=" + idVisaTransformable));

        if (visaTransformable.getDemandeur() == null || idDemandeur == null
                || !idDemandeur.equals(visaTransformable.getDemandeur().getIdDemandeur())) {
            throw new IllegalArgumentException("Le visa transformable selectionne n'appartient pas au demandeur.");
        }

        if (visaTransformable.getPassport() == null || idPassport == null
                || !idPassport.equals(visaTransformable.getPassport().getIdPassport())) {
            throw new IllegalArgumentException("Le visa transformable selectionne n'appartient pas au passeport.");
        }

        return visaTransformable;
    }

    private TypeVisa chargerTypeVisa(Integer idTypeVisa) {
        if (idTypeVisa == null) {
            throw new IllegalArgumentException("Le type de visa est obligatoire.");
        }

        return typeVisaRepository.findById(idTypeVisa)
                .orElseThrow(() -> new IllegalArgumentException("Type de visa introuvable pour id=" + idTypeVisa));
    }

    private void remplacerPiecesJointes(Integer idDemande, List<DemandeDTO.PieceJointeDTO> pieces,
            List<MultipartFile> files) {
        List<PieceJointe> piecesExistantes = pieceJointeRepository.findByIdDemande(idDemande);
        java.util.Set<Integer> idsRemplaces = (pieces == null ? Collections.<DemandeDTO.PieceJointeDTO>emptyList()
                : pieces)
                .stream()
                .map(DemandeDTO.PieceJointeDTO::getIdPieceAFournir)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (PieceJointe pieceExistante : piecesExistantes) {
            if (idsRemplaces.contains(pieceExistante.getIdPieceAFournir())) {
                supprimerFichierPieceJointe(pieceExistante.getLien());
                pieceJointeRepository.delete(pieceExistante);
            }
        }

        savePiecesJointes(pieces, files, idDemande);
    }

    private DemandeNouveauTitre chargerSourceNouveauTitre(Demande demande) {
        if (demande == null) {
            return null;
        }

        if (demande.getDemandeNouveauTitre() != null) {
            return demande.getDemandeNouveauTitre();
        }

        if (demande.getDemandeDuplicataCarteResident() != null) {
            return demande.getDemandeDuplicataCarteResident().getDemandeNouveauTitreSource();
        }

        if (demande.getDemandeTransfertVisa() != null) {
            return demande.getDemandeTransfertVisa().getDemandeNouveauTitreSource();
        }

        return demandeNouveauTitreRepository.findById(demande.getId()).orElse(null);
    }

    private void mettreAJourDemandeNouveauTitreSource(DemandeNouveauTitre demandeNouveauTitre, DemandeDTO dto) {
        if (demandeNouveauTitre == null || dto == null) {
            throw new IllegalArgumentException("La demande nouveau titre est obligatoire.");
        }

        Demande demandeSource = demandeNouveauTitre.getDemande();
        if (demandeSource == null) {
            throw new IllegalArgumentException("La demande source est introuvable.");
        }

        if (!isBlank(dto.getNumero()) && !dto.getNumero().trim().equals(demandeSource.getNumero())) {
            throw new IllegalArgumentException("Le numero de la demande source ne peut pas etre modifie.");
        }

        Demandeur demandeur = chargerDemandeur(dto.getIdDemandeur());
        Passport passport = chargerPassport(dto.getIdPassport(), dto.getIdDemandeur());
        VisaTransformable visaTransformable = chargerVisaTransformable(dto.getIdVisaTransformable(),
            dto.getIdDemandeur(),
            dto.getIdPassport());
        TypeVisa typeVisa = chargerTypeVisa(dto.getIdTypeVisa());

        demandeur = mettreAJourDemandeur(demandeur, dto.getDemandeur());
        passport = mettreAJourPassport(passport, dto.getPassport());
        visaTransformable = mettreAJourVisaTransformable(visaTransformable, dto.getVisaTransformable());

        demandeSource.setDemandeur(demandeur);
        demandeSource.setTypeDemande(typeDemandeRepository.findById(ID_TYPE_DEMANDE_NOUVEAU_TITRE)
                .orElseThrow(() -> new IllegalArgumentException("Type de demande introuvable pour nouveau titre.")));
        demandeRepository.save(demandeSource);

        demandeNouveauTitre.setPassport(passport);
        demandeNouveauTitre.setVisaTransformable(visaTransformable);
        demandeNouveauTitre.setTypeVisa(typeVisa);
        demandeNouveauTitreRepository.save(demandeNouveauTitre);
    }

    private boolean sontChampsOptionnelsComplets(DemandeNouveauTitre demandeNouveauTitre) {
        if (demandeNouveauTitre == null) {
            return false;
        }

        Demande demandeSource = demandeNouveauTitre.getDemande();
        Demandeur demandeur = demandeSource != null ? demandeSource.getDemandeur() : null;
        Passport passport = demandeNouveauTitre.getPassport();
        VisaTransformable visaTransformable = demandeNouveauTitre.getVisaTransformable();

        return demandeur != null
                && !isBlank(demandeur.getPrenom())
                && !isBlank(demandeur.getNomJeuneFille())
                && !isBlank(demandeur.getAdresseMada())
                && !isBlank(demandeur.getEmail())
                && passport != null
                && passport.getDateDelivrance() != null
                && passport.getDateExpiration() != null
                && visaTransformable != null
                && !isBlank(visaTransformable.getLieuEntreeMada())
                && visaTransformable.getDateSortie() != null;
    }

    private void supprimerFichierPieceJointe(String lien) {
        if (isBlank(lien) || !lien.startsWith("/uploads/")) {
            return;
        }

        Path relative = Paths.get(lien.substring("/uploads/".length()));
        Path fichier = uploadRoot.resolve(relative);
        try {
            Files.deleteIfExists(fichier);
        } catch (IOException e) {
            // ignore cleanup failures
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Demandeur mettreAJourDemandeur(Demandeur demandeur, DemandeurDTO dto) {
        if (dto == null) {
            return demandeur;
        }

        if (isBlank(dto.getNom())) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (dto.getDateNaissance() == null) {
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
        }
        if (isBlank(dto.getTelephone())) {
            throw new IllegalArgumentException("Le telephone est obligatoire.");
        }
        if (dto.getIdNationalite() == null) {
            throw new IllegalArgumentException("La nationalite est obligatoire.");
        }
        if (dto.getIdStatutFamilial() == null) {
            throw new IllegalArgumentException("La situation familiale est obligatoire.");
        }

        Nationnalite nationnalite = nationnaliteRepository.findById(dto.getIdNationalite())
                .orElseThrow(() -> new IllegalArgumentException("Nationalite introuvable pour id=" + dto.getIdNationalite()));

        SituationFamiliale situationFamiliale = situationFamilialeRepository.findById(dto.getIdStatutFamilial())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Situation familiale introuvable pour id=" + dto.getIdStatutFamilial()));

        demandeur.setNom(dto.getNom().trim());
        demandeur.setPrenom(dto.getPrenom());
        demandeur.setDateNaissance(dto.getDateNaissance());
        demandeur.setNomJeuneFille(dto.getNomJeuneFille());
        demandeur.setAdresseMada(dto.getAdresseMada());
        demandeur.setTelephone(dto.getTelephone().trim());
        demandeur.setEmail(dto.getEmail());
        demandeur.setNationnalite(nationnalite);
        demandeur.setSituationFamiliale(situationFamiliale);

        return demandeurRepository.save(demandeur);
    }

    private Passport mettreAJourPassport(Passport passport, PassportDTO dto) {
        if (dto == null) {
            return passport;
        }

        if (isBlank(dto.getNumero())) {
            throw new IllegalArgumentException("Le numero de passeport est obligatoire.");
        }
        if (dto.getDateDelivrance() != null && dto.getDateExpiration() != null
                && !dto.getDateExpiration().isAfter(dto.getDateDelivrance())) {
            throw new IllegalArgumentException("La date d'expiration doit etre apres la date de delivrance.");
        }

        passport.setNumero(dto.getNumero().trim());
        passport.setDateDelivrance(dto.getDateDelivrance());
        passport.setDateExpiration(dto.getDateExpiration());
        return passportRepository.save(passport);
    }

    private VisaTransformable mettreAJourVisaTransformable(VisaTransformable visaTransformable, VisaTransformableDTO dto) {
        if (visaTransformable == null || dto == null) {
            return visaTransformable;
        }

        if (isBlank(dto.getReferenceVisa())) {
            throw new IllegalArgumentException("La reference visa est obligatoire.");
        }
        if (dto.getDateEntreeMada() == null) {
            throw new IllegalArgumentException("La date d'entree Madagascar est obligatoire.");
        }

        visaTransformable.setReferenceVisa(dto.getReferenceVisa().trim());
        visaTransformable.setDateEntreeMada(dto.getDateEntreeMada());
        visaTransformable.setLieuEntreeMada(dto.getLieuEntreeMada());
        visaTransformable.setDateSortie(dto.getDateSortie());
        return visaTransformableRepository.save(visaTransformable);
    }

    private String obtenirNumeroDemande(String numero) {
        if (!isBlank(numero)) {
            String numeroNormalise = numero.trim();
            if (demandeRepository.existsByNumero(numeroNormalise)) {
                throw new IllegalArgumentException("Le numero de la demande est deja utilise.");
            }
            return numeroNormalise;
        }

        return genererNumeroDemande();
    }

    private String genererNumeroDemande() {
        String numero;
        do {
            numero = "DEM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (demandeRepository.existsByNumero(numero));
        return numero;
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

            String nomOriginal = StringUtils
                    .cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
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
