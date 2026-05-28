-- ========================================
-- V2__Initial_Data_Auth.sql
-- ========================================
-- Description: Seed data for authentication (roles and admin user)
-- Author: System
-- Date: 2025-10-29
-- ========================================

-- Insert default roles
INSERT INTO roles(name) VALUES('ROLE_PARENT')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles(name) VALUES('ROLE_TEACHER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles(name) VALUES('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user (password: admin)
INSERT INTO users(username, email, phone, password)
VALUES('admin', 'admin@example.com', '0900000000', '$2a$10$HvpzVKWTLIfxOYDoTA2EiOGmIuh4aOPjAtqlF/OKyMymr5hxKYKQG')
ON CONFLICT DO NOTHING;

-- Assign all roles to admin user
INSERT INTO user_roles(user_id, role_id)
SELECT u.id as user_id, r.id as role_id
FROM users u
JOIN roles r ON r.name IN ('ROLE_PARENT', 'ROLE_TEACHER', 'ROLE_ADMIN')
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

