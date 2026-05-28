-- ========================================
-- V1__Initial_Schema_Auth.sql
-- ========================================
-- Description: Initial authentication schema
-- Author: System
-- Date: 2025-10-29
-- ========================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Table: roles
-- Stores available roles in the system
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE roles IS 'System roles for authorization';
CREATE INDEX IF NOT EXISTS idx_name ON roles (name);

-- Table: users
-- Stores user account information
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE users IS 'User accounts';
CREATE INDEX IF NOT EXISTS idx_email ON users (email);
CREATE INDEX IF NOT EXISTS uk_users_phone ON users (phone);

-- Table: user_roles
-- Many-to-many relationship between users and roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_roles IS 'User-Role relationship mapping';
CREATE INDEX IF NOT EXISTS idx_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_role_id ON user_roles (role_id);

DROP TRIGGER IF EXISTS trg_roles_set_updated_at ON roles;
CREATE TRIGGER trg_roles_set_updated_at
BEFORE UPDATE ON roles
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_users_set_updated_at ON users;
CREATE TRIGGER trg_users_set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

