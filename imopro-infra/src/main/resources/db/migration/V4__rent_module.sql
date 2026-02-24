CREATE TABLE IF NOT EXISTS rent (
    id TEXT PRIMARY KEY,
    contact_id TEXT NOT NULL,
    property_id TEXT NOT NULL,
    monthly_amount REAL,
    start_date TEXT NOT NULL,
    end_date TEXT,
    notes TEXT,
    FOREIGN KEY(contact_id) REFERENCES contact(id),
    FOREIGN KEY(property_id) REFERENCES property(id)
);

CREATE TABLE IF NOT EXISTS rent_task_rule (
    id TEXT PRIMARY KEY,
    rent_id TEXT NOT NULL,
    frequency TEXT NOT NULL,
    auto_renew INTEGER NOT NULL DEFAULT 0,
    day_of_week INTEGER,
    day_of_month INTEGER,
    month_of_year INTEGER,
    title_prefix TEXT,
    description_prefix TEXT,
    last_generated_at TEXT,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(rent_id) REFERENCES rent(id)
);

ALTER TABLE task ADD COLUMN rent_id TEXT REFERENCES rent(id);
ALTER TABLE document ADD COLUMN rent_id TEXT REFERENCES rent(id);

CREATE INDEX IF NOT EXISTS idx_rent_contact ON rent(contact_id);
CREATE INDEX IF NOT EXISTS idx_rent_property ON rent(property_id);
CREATE INDEX IF NOT EXISTS idx_task_rent ON task(rent_id);
CREATE INDEX IF NOT EXISTS idx_document_rent ON document(rent_id);
