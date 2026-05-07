package com.example.demo.metier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.metier.dto.DemandeDetailDTO;
import com.example.demo.metier.dto.DemandeListeItemDTO;
import com.example.demo.metier.dto.DemandeSuiviDTO;
import com.example.demo.metier.dto.DemandeSuiviHistoriqueDTO;
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
import com.example.demo.model.VisaTransformable;
import com.example.demo.repository.DemandeNouveauTitreRepository;
import com.example.demo.repository.DemandeDuplicataCarteResidentRepository;
import com.example.demo.repository.DemandeRepository;
import com.example.demo.repository.DemandeTransfertVisaRepository;
import com.example.demo.repository.HistoriqueStatutDemandeRepository;
import com.example.demo.repository.PassportRepository;
import com.example.demo.repository.PieceAFournirRepository;
import com.example.demo.repository.PieceJointeRepository;
import com.example.demo.repository.StatutDemandeRepository;

@Service
public class DemandeListeService {

    private final DemandeRepository demandeRepository;
    private final DemandeNouveauTitreRepository demandeNouveauTitreRepository;
    private final DemandeDuplicataCarteResidentRepository demandeDuplicataCarteResidentRepository;
    private final DemandeTransfertVisaRepository demandeTransfertVisaRepository;
    private final HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final PieceAFournirRepository pieceAFournirRepository;
    private final PassportRepository passportRepository;

    public DemandeListeService(
            DemandeRepository demandeRepository,
            DemandeNouveauTitreRepository demandeNouveauTitreRepository,
            DemandeDuplicataCarteResidentRepository demandeDuplicataCarteResidentRepository,
            DemandeTransfertVisaRepository demandeTransfertVisaRepository,
            HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository,
            StatutDemandeRepository statutDemandeRepository,
            PieceJointeRepository pieceJointeRepository,
            PieceAFournirRepository pieceAFournirRepository,
            PassportRepository passportRepository) {
        this.demandeRepository = demandeRepository;
        this.demandeNouveauTitreRepository = demandeNouveauTitreRepository;
        this.demandeDuplicataCarteResidentRepository = demandeDuplicataCarteResidentRepository;
        this.demandeTransfertVisaRepository = demandeTransfertVisaRepository;
        this.historiqueStatutDemandeRepository = historiqueStatutDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.pieceJointeRepository = pieceJointeRepository;
        this.pieceAFournirRepository = pieceAFournirRepository;
        this.passportRepository = passportRepository;
    }

    public List<DemandeListeItemDTO> listerToutesLesDemandes() {
        return demandeRepository.findAll().stream()
                .map(demande -> new DemandeListeItemDTO(
                        demande.getId(),
                        demande.getNumero(),
                        construireNomDemandeur(demande.getDemandeur()),
                        getTypeVisaLibelle(demande),
                        demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null,
                        getStatutActuelLibelle(demande.getId()),
                        demande.getDateDemande()))
                .toList();
    }

    public DemandeDetailDTO getDetailDemande(Integer idDemande) {
        if (idDemande == null) {
            throw new IllegalArgumentException("L'id de la demande est obligatoire.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable pour id=" + idDemande));

        List<PieceJointe> piecesJointes = pieceJointeRepository.findByIdDemande(idDemande);
        List<Integer> idsPiecesAFournir = piecesJointes.stream()
                .map(PieceJointe::getIdPieceAFournir)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, String> nomsPiecesParId = idsPiecesAFournir.isEmpty()
                ? Collections.emptyMap()
                : pieceAFournirRepository.findAllById(idsPiecesAFournir).stream()
                        .collect(Collectors.toMap(PieceAFournir::getId, PieceAFournir::getNom, (a, b) -> a));

        List<DemandeDetailDTO.PieceJointeDetailDTO> piecesJointesDetail = piecesJointes.stream()
                .map(piece -> new DemandeDetailDTO.PieceJointeDetailDTO(
                        piece.getId(),
                        piece.getIdPieceAFournir(),
                        nomsPiecesParId.get(piece.getIdPieceAFournir()),
                        piece.getLien()))
                .toList();

        return new DemandeDetailDTO(
                demande.getId(),
                demande.getNumero(),
                getIdTypeVisa(demande),
                demande.getTypeDemande() != null ? demande.getTypeDemande().getId() : null,
                demande.getDateDemande(),
                getStatutActuelLibelle(demande.getId()),
                getTypeVisaLibelle(demande),
                demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null,
                toDemandeurDetail(demande.getDemandeur()),
                toPassportDetail(getPassport(demande)),
                toVisaTransformableDetail(getVisaTransformable(demande)),
                piecesJointesDetail);
    }

    public List<DemandeSuiviDTO> toListDemandeSuiviDTO(List<Demande> demandesLieesDemandeur) {
        List<DemandeSuiviDTO> demandeSuiviDTOs = new ArrayList<>();

        for (Demande demande : demandesLieesDemandeur) {

            DemandeListeItemDTO demandeListeItemDTO = new DemandeListeItemDTO(
                    demande.getId(),
                    demande.getNumero(),
                    construireNomDemandeur(demande.getDemandeur()),
                    getTypeVisaLibelle(demande),
                    demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null,
                    getStatutActuelLibelle(demande.getId()),
                    demande.getDateDemande());
            List<HistoriqueStatutDemande> historiqueStatutDemandes = historiqueStatutDemandeRepository
                    .findByIdDemandeOrderByDateHeureHistoriqueAsc(demande.getId());
            List<DemandeSuiviHistoriqueDTO> demandeSuiviHistoriqueDTOs = new ArrayList<>();
            for (HistoriqueStatutDemande historiqueStatutDemande : historiqueStatutDemandes) {
                StatutDemande statutDemande = statutDemandeRepository
                        .findById(historiqueStatutDemande.getStatutDemande()).get();
                DemandeSuiviHistoriqueDTO demandeSuiviHistoriqueDTO = new DemandeSuiviHistoriqueDTO(
                        historiqueStatutDemande.getStatutDemande(), statutDemande.getLibelle(),
                        historiqueStatutDemande.getDateHeureHistorique());
                demandeSuiviHistoriqueDTOs.add(demandeSuiviHistoriqueDTO);
            }
            demandeSuiviDTOs.add(new DemandeSuiviDTO(demandeListeItemDTO, demandeSuiviHistoriqueDTOs));
        }
        return demandeSuiviDTOs;
    }

    public List<DemandeSuiviDTO> suivreDemande(String numero) {
        if (numero == null) {
            throw new IllegalArgumentException("L'identifiant de suivi est obligatoire.");
        }

        Optional<Demande> demandeRecherchee = demandeRepository.findByNumero(numero);
        if (demandeRecherchee.isPresent()) {
            Demande demandeRechercheeValue = demandeRecherchee.get();
            Demandeur associe = demandeRechercheeValue.getDemandeur();
            List<Demande> demandesLieesDemandeur = new ArrayList<>(
                    demandeRepository.findByDemandeur_IdDemandeurOrderByDateDemandeAsc(
                            associe.getIdDemandeur()).stream()
                        .filter(demande -> !Objects.equals(demande.getId(), demandeRecherchee.get().getId()))
                        .toList()
            );
            demandesLieesDemandeur.add(0, demandeRechercheeValue);
            return toListDemandeSuiviDTO(demandesLieesDemandeur);
        }

        Passport passport = passportRepository.findByNumero(numero)
                .orElseThrow(
                        () -> new IllegalArgumentException("Aucune demande ou passeport trouve pour id=" + numero));

        List<Demande> demandesLieesPassport = trouverDemandeReferencePourPassport(passport.getIdPassport());
        return toListDemandeSuiviDTO(demandesLieesPassport);

    }

    private List<Demande> trouverDemandeReferencePourPassport(Integer idPassportRecherche) {
        if (idPassportRecherche == null) {
            return new ArrayList<>();
        }

        return demandeRepository.findAll().stream()
                .filter(demande -> Objects.equals(getPassportIdLieALaDemande(demande), idPassportRecherche))
                .sorted(Comparator.comparing(Demande::getDateDemande, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private Integer getPassportIdLieALaDemande(Demande demande) {
        if (demande == null) {
            return null;
        }

        DemandeNouveauTitre demandeNouveauTitre = demande.getDemandeNouveauTitre();
        if (demandeNouveauTitre == null) {
            demandeNouveauTitre = demandeNouveauTitreRepository.findById(demande.getId()).orElse(null);
        }
        if (demandeNouveauTitre != null && demandeNouveauTitre.getPassport() != null) {
            return demandeNouveauTitre.getPassport().getIdPassport();
        }

        DemandeTransfertVisa transfertVisa = demande.getDemandeTransfertVisa();
        if (transfertVisa == null) {
            transfertVisa = demandeTransfertVisaRepository.findById(demande.getId()).orElse(null);
        }
        if (transfertVisa != null && transfertVisa.getPassport() != null) {
            return transfertVisa.getPassport().getIdPassport();
        }

        DemandeDuplicataCarteResident duplicataCarteResident = demande.getDemandeDuplicataCarteResident();
        if (duplicataCarteResident == null) {
            duplicataCarteResident = demandeDuplicataCarteResidentRepository.findById(demande.getId()).orElse(null);
        }
        if (duplicataCarteResident != null && duplicataCarteResident.getDemandeNouveauTitreSource() != null
                && duplicataCarteResident.getDemandeNouveauTitreSource().getPassport() != null) {
            return duplicataCarteResident.getDemandeNouveauTitreSource().getPassport().getIdPassport();
        }

        return null;
    }

    private String construireNomDemandeur(Demandeur demandeur) {
        if (demandeur == null) {
            return null;
        }

        String nom = demandeur.getNom() == null ? "" : demandeur.getNom().trim();
        String prenom = demandeur.getPrenom() == null ? "" : demandeur.getPrenom().trim();
        String nomComplet = (nom + " " + prenom).trim();
        return nomComplet.isEmpty() ? null : nomComplet;
    }

    private String getStatutActuelLibelle(Integer idDemande) {
        if (idDemande == null) {
            return null;
        }

        Optional<HistoriqueStatutDemande> historique = historiqueStatutDemandeRepository
                .findTopByIdDemandeOrderByDateHeureHistoriqueDesc(idDemande);

        if (historique.isEmpty()) {
            return null;
        }

        Integer idStatut = historique.get().getStatutDemande();
        if (idStatut == null) {
            return null;
        }

        return statutDemandeRepository.findById(idStatut)
                .map(statut -> statut.getLibelle())
                .orElse(null);
    }

    private DemandeDetailDTO.DemandeurDetailDTO toDemandeurDetail(Demandeur demandeur) {
        if (demandeur == null) {
            return null;
        }

        return new DemandeDetailDTO.DemandeurDetailDTO(
                demandeur.getIdDemandeur(),
                demandeur.getNationnalite() == null ? null : demandeur.getNationnalite().getId(),
                demandeur.getSituationFamiliale() == null ? null : demandeur.getSituationFamiliale().getId(),
                demandeur.getNom(),
                demandeur.getPrenom(),
                demandeur.getDateNaissance(),
                demandeur.getNomJeuneFille(),
                demandeur.getAdresseMada(),
                demandeur.getEmail(),
                demandeur.getTelephone());
    }

    private DemandeDetailDTO.PassportDetailDTO toPassportDetail(Passport passport) {
        if (passport == null) {
            return null;
        }

        return new DemandeDetailDTO.PassportDetailDTO(
                passport.getIdPassport(),
                passport.getNumero(),
                passport.getDateDelivrance(),
                passport.getDateExpiration());
    }

    private DemandeDetailDTO.VisaTransformableDetailDTO toVisaTransformableDetail(VisaTransformable visaTransformable) {
        if (visaTransformable == null) {
            return null;
        }

        return new DemandeDetailDTO.VisaTransformableDetailDTO(
                visaTransformable.getId(),
                visaTransformable.getReferenceVisa(),
                visaTransformable.getDateEntreeMada(),
                visaTransformable.getLieuEntreeMada(),
                visaTransformable.getDateSortie());
    }

    private String getTypeVisaLibelle(Demande demande) {
        DemandeNouveauTitre demandeNouveauTitre = getDemandeNouveauTitre(demande);
        if (demandeNouveauTitre == null || demandeNouveauTitre.getTypeVisa() == null) {
            return null;
        }

        return demandeNouveauTitre.getTypeVisa().getLibelle();
    }

    private Integer getIdTypeVisa(Demande demande) {
        DemandeNouveauTitre demandeNouveauTitre = getDemandeNouveauTitre(demande);
        if (demandeNouveauTitre == null || demandeNouveauTitre.getTypeVisa() == null) {
            return null;
        }

        return demandeNouveauTitre.getTypeVisa().getId();
    }

    private Passport getPassport(Demande demande) {
        if (demande != null && demande.getTypeDemande() != null && demande.getTypeDemande().getId() != null) {
            if (Integer.valueOf(3).equals(demande.getTypeDemande().getId())) {
                return getPassportTransfert(demande.getId());
            }
        }

        DemandeNouveauTitre demandeNouveauTitre = getDemandeNouveauTitre(demande);
        return demandeNouveauTitre != null ? demandeNouveauTitre.getPassport() : null;
    }

    private Passport getPassportTransfert(Integer idDemande) {
        if (idDemande == null) {
            return null;
        }

        DemandeTransfertVisa transfert = demandeTransfertVisaRepository.findById(idDemande).orElse(null);
        return transfert != null ? transfert.getPassport() : null;
    }

    private VisaTransformable getVisaTransformable(Demande demande) {
        DemandeNouveauTitre demandeNouveauTitre = getDemandeNouveauTitre(demande);
        return demandeNouveauTitre != null ? demandeNouveauTitre.getVisaTransformable() : null;
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
}
