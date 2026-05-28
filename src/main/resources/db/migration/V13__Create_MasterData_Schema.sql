-- ========================================
-- V13__Create_MasterData_Schema.sql
-- ========================================
-- Generic master_data lookup table. Holds enumerated values for multiple
-- domains (tool, subject, location, room) keyed by (type, code) so future
-- enums don't each need their own table.
-- ========================================

CREATE TABLE IF NOT EXISTS master_data (
    id          BIGSERIAL    PRIMARY KEY,
    type        VARCHAR(50)  NOT NULL,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    position    INTEGER      NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    metadata    TEXT,                                 -- JSON string via MapToJsonConverter
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    version     BIGINT       DEFAULT 0,

    CONSTRAINT uq_master_data_type_code UNIQUE (type, code)
);

CREATE INDEX IF NOT EXISTS idx_master_data_type_active_position
    ON master_data(type, active, position);

-- Seed tools used by the Course form. Codes are lowercase; names hold the brand casing.
INSERT INTO master_data (type, code, name, position, active) VALUES
    ('tool', 'mtiny',   'mTiny',   1, TRUE),
    ('tool', 'scratch', 'Scratch', 2, TRUE),
    ('tool', 'mbot2',   'mBot2',   3, TRUE),
    ('tool', 'techai',  'TechAI',  4, TRUE),
    ('tool', 'violin',  'Violin',  5, TRUE);
