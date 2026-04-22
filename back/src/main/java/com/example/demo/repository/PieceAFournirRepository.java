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
      WHERE p.typeVisa IS NOT NULL
        AND p.typeVisa.id = :idTypeVisa
      """)
    List<PieceAFournir> findByTypeVisa_IdTypeVisa(@Param("idTypeVisa") Integer idTypeVisa);

    @Query("""
      SELECT p
      FROM PieceAFournir p
      WHERE p.typeDemande IS NOT NULL
        AND p.typeDemande.id = :idTypeDemande
      """)
    List<PieceAFournir> findByTypeDemande_IdTypeDemande(@Param("idTypeDemande") Integer idTypeDemande);

    List<PieceAFournir> findByTypeVisaIsNullAndTypeDemandeIsNull();

    @Query("""
            SELECT p
            FROM PieceAFournir p
            WHERE p.obligatoire = true
              AND (p.typeVisa IS NULL OR p.typeVisa.id = :idTypeVisa)
              AND (p.typeDemande IS NULL OR p.typeDemande.id = :idTypeDemande)
            """)
            List<PieceAFournir> findPiecesObligatoires(@Param("idTypeVisa") Integer idTypeVisa,
              @Param("idTypeDemande") Integer idTypeDemande);
}
