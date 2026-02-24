INSERT INTO pipeline_stage (name, position)
SELECT 'Lead', 1
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 1);

INSERT INTO pipeline_stage (name, position)
SELECT 'Estimation', 2
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 2);

INSERT INTO pipeline_stage (name, position)
SELECT 'Mandat', 3
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 3);

INSERT INTO pipeline_stage (name, position)
SELECT 'En vente', 4
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 4);

INSERT INTO pipeline_stage (name, position)
SELECT 'Visites', 5
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 5);

INSERT INTO pipeline_stage (name, position)
SELECT 'Offre', 6
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 6);

INSERT INTO pipeline_stage (name, position)
SELECT 'Compromis', 7
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 7);

INSERT INTO pipeline_stage (name, position)
SELECT 'Vendu/Clos', 8
WHERE NOT EXISTS (SELECT 1 FROM pipeline_stage WHERE position = 8);
