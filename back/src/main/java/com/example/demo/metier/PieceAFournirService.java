package com.example.demo.metier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.metier.dto.PieceAFournirParVisaDTO;
import com.example.demo.model.PieceAFournir;
import com.example.demo.repository.PieceAFournirRepository;

@Service
public class PieceAFournirService {

    private static final String CATEGORIE_COMMUNE = "COMMUNE";
    private static final String CATEGORIE_SPECIFIQUE = "SPECIFIQUE";

    private final PieceAFournirRepository pieceAFournirRepository;

    public PieceAFournirService(PieceAFournirRepository pieceAFournirRepository) {
        this.pieceAFournirRepository = pieceAFournirRepository;
    }

    public List<PieceAFournirParVisaDTO> getPiecesParTypeVisa(Integer idTypeVisa) {
        if (idTypeVisa == null) {
            throw new IllegalArgumentException("Le type de visa est obligatoire.");
        }

        List<PieceAFournir> piecesCommunes = pieceAFournirRepository.findByTypeVisaIsNullAndTypeDemandeIsNull();
        List<PieceAFournir> piecesSpecifiques = pieceAFournirRepository.findByTypeVisa_IdTypeVisa(idTypeVisa);

        Map<Integer, PieceAFournirParVisaDTO> piecesFusionnees = new LinkedHashMap<>();

        for (PieceAFournir piece : piecesCommunes) {
            piecesFusionnees.put(piece.getId(), toDto(piece, CATEGORIE_COMMUNE));
        }

        for (PieceAFournir piece : piecesSpecifiques) {
            piecesFusionnees.put(piece.getId(), toDto(piece, CATEGORIE_SPECIFIQUE));
        }

        return new ArrayList<>(piecesFusionnees.values());
    }

    private PieceAFournirParVisaDTO toDto(PieceAFournir piece, String categorie) {
        return new PieceAFournirParVisaDTO(
                piece.getId(),
                piece.getNom(),
                piece.getObligatoire(),
                categorie);
    }
}
