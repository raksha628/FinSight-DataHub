-- ═══════════════════════════════════════════════════════════════════════════
-- V5 — Create ETFs Table
-- Exchange Traded Funds daily data including NAV and AUM.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE etfs (
    id              BIGSERIAL       PRIMARY KEY,
    symbol          VARCHAR(20)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    trade_date      DATE            NOT NULL,
    nav             NUMERIC(15, 4)  NOT NULL CHECK (nav > 0),
    open_price      NUMERIC(15, 4)  CHECK (open_price >= 0),
    high_price      NUMERIC(15, 4)  CHECK (high_price >= 0),
    low_price       NUMERIC(15, 4)  CHECK (low_price >= 0),
    close_price     NUMERIC(15, 4)  CHECK (close_price >= 0),
    volume          BIGINT          CHECK (volume >= 0),
    aum             NUMERIC(20, 2)  CHECK (aum >= 0),     -- Assets Under Management
    expense_ratio   NUMERIC(6, 4)   CHECK (expense_ratio >= 0),
    category        VARCHAR(100),   -- e.g. 'Large-Cap Blend', 'Technology'
    upload_id       BIGINT          REFERENCES upload_history(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_etfs_symbol_date UNIQUE (symbol, trade_date)
);

CREATE INDEX idx_etfs_symbol     ON etfs (symbol);
CREATE INDEX idx_etfs_trade_date ON etfs (trade_date DESC);
CREATE INDEX idx_etfs_category   ON etfs (category);
