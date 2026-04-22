INSERT INTO
   piece_jointe (fournie, id_piece_a_fournir, id_demande_effectuee)
VALUES
   (TRUE, 7, 2);

DELETE FROM Piece_jointe;

SELECT
   id_demande_effectuee,
   id_piece_a_fournir
FROM
   Piece_jointe;

SELECT
   *
FROM
   piece_a_fournir;

DELETE FROM piece_a_fournir;