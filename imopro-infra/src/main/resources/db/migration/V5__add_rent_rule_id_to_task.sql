-- Ajout de la colonne rent_rule_id à la table task pour lier les tâches à une règle de génération de tâches de loyer
ALTER TABLE task ADD COLUMN rent_rule_id TEXT REFERENCES rent_task_rule(id);

CREATE INDEX IF NOT EXISTS idx_task_rent_rule ON task(rent_rule_id);
