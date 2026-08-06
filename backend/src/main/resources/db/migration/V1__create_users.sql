-- ═══════════════════════════════════════════════════════════════════════════
-- V1 — Create Users Table
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'VIEWER'
                                    CHECK (role IN ('ADMIN','ANALYST','VIEWER')),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Index for login lookups by email and username
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role     ON users (role);

COMMENT ON TABLE  users               IS 'Application users with role-based access control';
COMMENT ON COLUMN users.role          IS 'ADMIN: full access | ANALYST: upload + AI | VIEWER: read-only';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password, strength 12';
