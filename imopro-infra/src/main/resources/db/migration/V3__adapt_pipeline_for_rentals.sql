-- Adaptation des étapes pipeline pour un flux location
UPDATE pipeline_stage SET name = 'Prospect locataire' WHERE position = 1;
UPDATE pipeline_stage SET name = 'Dossier reçu' WHERE position = 2;
UPDATE pipeline_stage SET name = 'Visite planifiée' WHERE position = 3;
UPDATE pipeline_stage SET name = 'Dossier complet' WHERE position = 4;
UPDATE pipeline_stage SET name = 'Bail à signer' WHERE position = 5;
UPDATE pipeline_stage SET name = 'Entrée locataire' WHERE position = 6;
UPDATE pipeline_stage SET name = 'Loué/Clos' WHERE position = 7;

-- Conserver 8 colonnes en renommant l'ancienne étape finale
UPDATE pipeline_stage SET name = 'Archive' WHERE position = 8;

-- Remap des statuts historiques vers le nouveau pipeline location
UPDATE property
SET status = CASE status
    WHEN 'Lead' THEN 'Prospect locataire'
    WHEN 'Estimation' THEN 'Dossier reçu'
    WHEN 'Mandat' THEN 'Visite planifiée'
    WHEN 'En vente' THEN 'Dossier complet'
    WHEN 'Visites' THEN 'Bail à signer'
    WHEN 'Offre' THEN 'Entrée locataire'
    WHEN 'Compromis' THEN 'Loué/Clos'
    WHEN 'Vendu/Clos' THEN 'Loué/Clos'
    ELSE status
END;
