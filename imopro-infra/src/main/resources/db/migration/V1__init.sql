CREATE TABLE contact (
    id TEXT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    notes TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE property (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    address TEXT,
    city TEXT,
    postal_code TEXT,
    property_type TEXT,
    surface REAL,
    rooms INTEGER,
    price REAL,
    status TEXT,
    owner_contact_id TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(owner_contact_id) REFERENCES contact(id)
);

CREATE TABLE task (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL,
    due_date TEXT,
    created_at TEXT NOT NULL,
    completed_at TEXT
);

CREATE TABLE document (
    id TEXT PRIMARY KEY,
    file_name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    mime_type TEXT,
    size_bytes INTEGER,
    contact_id TEXT,
    property_id TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY(contact_id) REFERENCES contact(id),
    FOREIGN KEY(property_id) REFERENCES property(id)
);

CREATE TABLE interaction (
    id TEXT PRIMARY KEY,
    contact_id TEXT,
    property_id TEXT,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(contact_id) REFERENCES contact(id),
    FOREIGN KEY(property_id) REFERENCES property(id)
);

CREATE TABLE tag (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE contact_tag (
    contact_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    PRIMARY KEY (contact_id, tag_id),
    FOREIGN KEY(contact_id) REFERENCES contact(id),
    FOREIGN KEY(tag_id) REFERENCES tag(id)
);

CREATE TABLE property_tag (
    property_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    PRIMARY KEY (property_id, tag_id),
    FOREIGN KEY(property_id) REFERENCES property(id),
    FOREIGN KEY(tag_id) REFERENCES tag(id)
);

CREATE TABLE document_tag (
    document_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    PRIMARY KEY (document_id, tag_id),
    FOREIGN KEY(document_id) REFERENCES document(id),
    FOREIGN KEY(tag_id) REFERENCES tag(id)
);

CREATE TABLE pipeline_stage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    position INTEGER NOT NULL
);

CREATE TABLE pipeline_event (
    id TEXT PRIMARY KEY,
    property_id TEXT NOT NULL,
    stage_id INTEGER NOT NULL,
    changed_at TEXT NOT NULL,
    notes TEXT,
    FOREIGN KEY(property_id) REFERENCES property(id),
    FOREIGN KEY(stage_id) REFERENCES pipeline_stage(id)
);

CREATE INDEX idx_contact_name ON contact(last_name, first_name);
CREATE INDEX idx_contact_email ON contact(email);
CREATE INDEX idx_contact_phone ON contact(phone);
CREATE INDEX idx_property_status ON property(status);
CREATE INDEX idx_property_address ON property(address);
CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_due_date ON task(due_date);
CREATE INDEX idx_pipeline_property ON pipeline_event(property_id);
