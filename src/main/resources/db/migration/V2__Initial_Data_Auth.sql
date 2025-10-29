-- ========================================
-- V2__Initial_Data_Auth.sql
-- ========================================
-- Description: Seed data for authentication (roles and admin user)
-- Author: System
-- Date: 2025-10-29
-- ========================================

-- Insert default roles
INSERT INTO roles(name) VALUES('ROLE_USER')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO roles(name) VALUES('ROLE_MODERATOR')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO roles(name) VALUES('ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name=name;

-- Insert default admin user (password: admin)
INSERT INTO users(username, email, password)
VALUES('admin', 'admin@example.com', '$2a$10$HvpzVKWTLIfxOYDoTA2EiOGmIuh4aOPjAtqlF/OKyMymr5hxKYKQG')
ON DUPLICATE KEY UPDATE email=email;

-- Assign all roles to admin user
INSERT INTO user_roles(user_id, role_id)
SELECT u.id as user_id, r.id as role_id
FROM users u, roles r
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id=user_id;

