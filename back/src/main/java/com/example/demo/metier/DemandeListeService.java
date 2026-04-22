package com.example.demo.metier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.metier.dto.DemandeDetailDTO;
import com.example.demo.metier.dto.DemandeListeItemDTO;
import com.example.demo.model.DemandeEffectue;
import com.example.demo.model.Demandeur;
import com.example.demo.model.HistoriqueStatutDemande;
import com.example.demo.model.Passport;
import com.example.demo.model.PieceAFournir;
import com.example.demo.model.PieceJointe;
import com.example.demo.model.VisaTransformable;
import com.example.demo.repository.DemandeEffectueRepository;
import com.example.demo.repository.HistoriqueStatutDemandeRepository;
import com.example.demo.repository.PieceAFournirRepository;
import com.example.demo.repository.PieceJointeRepository;
import com.example.demo.repository.StatutDemandeRepository;

@Service
public class DemandeListeService {

    private final DemandeEffectueRepository demandeEffectueRepository;
    private final HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final PieceAFournirRepository pieceAFournirRepository;

    public DemandeListeService(
            DemandeEffectueRepository demandeEffectueRepository,
            HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository,
            StatutDemandeRepository statutDemandeRepository,
            PieceJointeRepository pieceJointeRepository,
            PieceAFournirRepository pieceAFournirRepository) {
        this.demandeEffectueRepository = demandeEffectueRepository;
        this.historiqueStatutDemandeRepository = historiqueStatutDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.pieceJointeRepository = pieceJointeRepository;
        this.pieceAFournirRepository = pieceAFournirRepository;
    }

    public List<DemandeListeItemDTO> listerToutesLesDemandes() {
        return demandeEffectueRepository.findAll().stream()
                .map(demande -> new DemandeListeItemDTO(
                        demande.getId(),
                        construireNomDemandeur(demande.getDemandeur()),
                        demande.getTypeVisa() != null ? demande.getTypeVisa().getLibelle() : null,
                        demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null,
                        getStatutActuelLibelle(demande.getId()),
                        demande.getDateDemande()))
                .toList();
    }

    public DemandeDetailDTO getDetailDemande(Integer idDemande) {
        if (idDemande == null) {
            throw new IllegalArgumentException("L'id de la demande est obligatoire.");
        }

        DemandeEffectue demande = demandeEffectueRepository.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable pour id=" + idDemande));

        List<PieceJointe> piecesJointes = pieceJointeRepository.findByIdDemandeEffectuee(idDemande);
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
                        piece.getFournie()))
                .toList();

        return new DemandeDetailDTO(
                demande.getId(),
                demande.getDateDemande(),
                getStatutActuelLibelle(demande.getId()),
                demande.getTypeVisa() != null ? demande.getTypeVisa().getLibelle() : null,
                demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null,
                toDemandeurDetail(demande.getDemandeur()),
                toPassportDetail(demande.getPassport()),
                toVisaTransformableDetail(demande.getVisaTransformable()),
                piecesJointesDetail);
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
                .findTopByIdDemandeEffectueeOrderByDateHeureHistoriqueDesc(idDemande);

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
                demandeur.getNom(),
                demandeur.getPrenom(),
                demandeur.getDateNaissance(),
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
}
