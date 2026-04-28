INSERT INTO
    piece_jointe (id_piece_a_fournir, id_demande)
VALUES
    (7, 2);

DELETE FROM Piece_jointe;

SELECT
    id_demande,
    id_piece_a_fournir
FROM
    Piece_jointe;

SELECT
    *
FROM
    piece_a_fournir;

DELETE FROM piece_a_fournir;

ALTER TABLE Piece_jointe DROP COLUMN fournie;

ALTER TABLE piece_a_fournir DROP COLUMN nom ;
ALTER TABLE piece_a_fournir ADD COLUMN nom TEXT ;
