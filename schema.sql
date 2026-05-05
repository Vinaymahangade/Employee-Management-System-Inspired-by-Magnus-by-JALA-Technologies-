-- ============================================================
-- JALA Employee Management System - PostgreSQL Schema
-- ============================================================

-- Create database (run as superuser if needed)
-- CREATE DATABASE emp_management_db;
-- \c emp_management_db;

-- Drop table if recreating
DROP TABLE IF EXISTS employees CASCADE;

-- ── Employees Table ────────────────────────────────────────
CREATE TABLE employees (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password      VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    status        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    phone         VARCHAR(20),
    department    VARCHAR(100),
    designation   VARCHAR(100),
    profile_image VARCHAR(255),
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── Indexes ────────────────────────────────────────────────
CREATE UNIQUE INDEX idx_employee_email  ON employees(email);
CREATE        INDEX idx_employee_status ON employees(status);
CREATE        INDEX idx_employee_role   ON employees(role);
CREATE        INDEX idx_employee_dept   ON employees(department);

-- ── Auto-update updated_at trigger ────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
BEFORE UPDATE ON employees
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ── Seed Data (optional - app also auto-seeds via DataSeeder) ──
INSERT INTO employees (name, email, password, role, status, department, designation, phone)
VALUES
  ('Super Admin',  'admin@jala.com', '$2a$10$examplehashforpasswordadmin123', 'ADMIN', 'ACTIVE', 'Management',   'System Administrator', '9999999999'),
  ('John Doe',     'john@jala.com',  '$2a$10$examplehashforpassworduser123',  'USER',  'ACTIVE', 'Engineering',  'Software Developer',   '8888888888'),
  ('Jane Smith',   'jane@jala.com',  '$2a$10$examplehashforpassworduser123',  'USER',  'INACTIVE','HR',          'HR Manager',           '7777777777');

-- NOTE: Use DataSeeder (Spring Boot) on first run — it properly BCrypt-hashes passwords.
-- The hashes above are placeholders. DataSeeder will handle real password encoding.

-- ── Verify ────────────────────────────────────────────────
SELECT id, name, email, role, status, department FROM employees;
