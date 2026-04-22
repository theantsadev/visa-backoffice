package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.PieceAFournir;

public interface PieceAFournirRepository extends JpaRepository<PieceAFournir, Integer> {

    @Query("""
            SELECT p
            FROM PieceAFournir p
            WHERE p.obligatoire = true
              AND (p.idTypeVisa IS NULL OR p.idTypeVisa = :idTypeVisa)
            AND (p.idTypeDemandeEffectuee IS NULL OR p.idTypeDemandeEffectuee = :idTypeDemande)
            """)
            List<PieceAFournir> findPiecesObligatoires(@Param("idTypeVisa") Integer idTypeVisa,
              @Param("idTypeDemande") Integer idTypeDemande);
}
