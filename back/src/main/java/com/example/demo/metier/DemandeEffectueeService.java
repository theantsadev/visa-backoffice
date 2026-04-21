package com.example.demo.metier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.metier.dto.DemandeDTO;
import com.example.demo.metier.dto.DemandeSoumiseDTO;
import com.example.demo.model.DemandeEffectue;
import com.example.demo.model.Demandeur;
import com.example.demo.model.HistoriqueStatutDemande;
import com.example.demo.model.Passport;
import com.example.demo.model.PieceAFournir;
import com.example.demo.model.PieceJointe;
import com.example.demo.model.StatutDemande;
import com.example.demo.model.TypeDemande;
import com.example.demo.model.TypeVisa;
import com.example.demo.model.VisaTransformable;
import com.example.demo.repository.DemandeEffectueRepository;
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

    private static final String STATUT_DOSSIER_CREE = "Dossier créé";

    private final DemandeurRepository demandeurRepository;
    private final PassportRepository passportRepository;
    private final VisaTransformableRepository visaTransformableRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final DemandeEffectueRepository demandeEffectueRepository;
    private final HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository;
    private final PieceAFournirRepository pieceAFournirRepository;
    private final PieceJointeRepository pieceJointeRepository;

    public DemandeEffectueeService(
            DemandeurRepository demandeurRepository,
            PassportRepository passportRepository,
            VisaTransformableRepository visaTransformableRepository,
            TypeVisaRepository typeVisaRepository,
            TypeDemandeRepository typeDemandeRepository,
            StatutDemandeRepository statutDemandeRepository,
            DemandeEffectueRepository demandeEffectueRepository,
            HistoriqueStatutDemandeRepository historiqueStatutDemandeRepository,
            PieceAFournirRepository pieceAFournirRepository,
            PieceJointeRepository pieceJointeRepository) {
        this.demandeurRepository = demandeurRepository;
        this.passportRepository = passportRepository;
        this.visaTransformableRepository = visaTransformableRepository;
        this.typeVisaRepository = typeVisaRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.demandeEffectueRepository = demandeEffectueRepository;
        this.historiqueStatutDemandeRepository = historiqueStatutDemandeRepository;
        this.pieceAFournirRepository = pieceAFournirRepository;
        this.pieceJointeRepository = pieceJointeRepository;
    }

    @Transactional
    public DemandeSoumiseDTO soumettreDemande(DemandeDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La demande est obligatoire.");
        }
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
        if (dto.getIdTypeDemande() == null) {
            throw new IllegalArgumentException("Le type de demande est obligatoire.");
        }

        Demandeur demandeur = demandeurRepository.findById(dto.getIdDemandeur())
                .orElseThrow(() -> new IllegalArgumentException("Demandeur introuvable pour id=" + dto.getIdDemandeur()));

        Passport passport = passportRepository.findById(dto.getIdPassport())
                .orElseThrow(() -> new IllegalArgumentException("Passeport introuvable pour id=" + dto.getIdPassport()));

        VisaTransformable visaTransformable = visaTransformableRepository.findById(dto.getIdVisaTransformable())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Visa transformable introuvable pour id=" + dto.getIdVisaTransformable()));

        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(() -> new IllegalArgumentException("Type de visa introuvable pour id=" + dto.getIdTypeVisa()));

        TypeDemande typeDemande = typeDemandeRepository.findById(dto.getIdTypeDemande())
                .orElseThrow(() -> new IllegalArgumentException("Type de demande introuvable pour id=" + dto.getIdTypeDemande()));

        validerPiecesObligatoires(dto.getPiecesJointes(), dto.getIdTypeVisa().intValue());

        DemandeEffectue demande = new DemandeEffectue();
        demande.setDateDemande(LocalDateTime.now());
        demande.setDemandeur(demandeur);
        demande.setPassport(passport);
        demande.setVisaTransformable(visaTransformable);
        demande.setTypeVisa(typeVisa);
        demande.setTypeDemande(typeDemande);

        DemandeEffectue demandeCreee = demandeEffectueRepository.save(demande);

        StatutDemande statutCree = statutDemandeRepository.findByLibelleIgnoreCase(STATUT_DOSSIER_CREE)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + STATUT_DOSSIER_CREE));

        HistoriqueStatutDemande historique = new HistoriqueStatutDemande();
        historique.setIdDemandeEffectuee(demandeCreee.getId());
        historique.setStatutDemande(statutCree.getId());
        historique.setDateHeureHistorique(LocalDateTime.now());
        historiqueStatutDemandeRepository.save(historique);

        savePiecesJointes(dto.getPiecesJointes(), demandeCreee.getId().intValue());

        return new DemandeSoumiseDTO(demandeCreee.getId(), statutCree.getLibelle(), demandeCreee.getDateDemande());
    }

    public void validerPiecesObligatoires(List<DemandeDTO.PieceJointeDTO> pieces, Integer idTypeVisa) {
        if (idTypeVisa == null) {
            throw new IllegalArgumentException("L'id type visa est obligatoire pour valider les pieces.");
        }

        List<DemandeDTO.PieceJointeDTO> safePieces = pieces == null ? Collections.emptyList() : pieces;

        Set<Long> piecesFournies = safePieces.stream()
                .filter(piece -> Boolean.TRUE.equals(piece.getFournie()))
                .map(DemandeDTO.PieceJointeDTO::getIdPieceAFournir)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        List<PieceAFournir> piecesObligatoires = pieceAFournirRepository.findPiecesObligatoiresByTypeVisa(idTypeVisa.longValue());

        for (PieceAFournir pieceObligatoire : piecesObligatoires) {
            if (!piecesFournies.contains(pieceObligatoire.getId())) {
                throw new IllegalArgumentException("Piece obligatoire manquante (id=" + pieceObligatoire.getId() + ").");
            }
        }
    }

    public void savePiecesJointes(List<DemandeDTO.PieceJointeDTO> pieces, Integer idDemande) {
        if (idDemande == null) {
            throw new IllegalArgumentException("L'id de la demande est obligatoire.");
        }

        if (pieces == null || pieces.isEmpty()) {
            return;
        }

        for (DemandeDTO.PieceJointeDTO piece : pieces) {
            if (piece == null || !Boolean.TRUE.equals(piece.getFournie()) || piece.getIdPieceAFournir() == null) {
                continue;
            }

            PieceJointe pieceJointe = new PieceJointe();
            pieceJointe.setFournie(true);
            pieceJointe.setIdPieceAFournir(piece.getIdPieceAFournir());
            pieceJointe.setIdDemandeEffectuee(idDemande.longValue());
            pieceJointeRepository.save(pieceJointe);
        }
    }
}
