-- ═══════════════════════════════════════════════════════════════════════════
-- V6 — Create Mutual Funds Table
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE mutual_funds (
    id          BIGSERIAL       PRIMARY KEY,
    symbol      VARCHAR(20)     NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    nav_date    DATE            NOT NULL,
    nav         NUMERIC(15, 4)  NOT NULL CHECK (nav > 0),
    category    VARCHAR(100),
    fund_house  VARCHAR(255),
    aum         NUMERIC(20, 2)  CHECK (aum >= 0),
    expense_ratio NUMERIC(6, 4) CHECK (expense_ratio >= 0),
    upload_id   BIGINT          REFERENCES upload_history(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_mutual_funds_symbol_date UNIQUE (symbol, nav_date)
);

CREATE INDEX idx_mutual_funds_symbol   ON mutual_funds (symbol);
CREATE INDEX idx_mutual_funds_nav_date ON mutual_funds (nav_date DESC);
CREATE INDEX idx_mutual_funds_category ON mutual_funds (category);
