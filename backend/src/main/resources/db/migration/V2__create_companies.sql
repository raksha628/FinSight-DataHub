-- ═══════════════════════════════════════════════════════════════════════════
-- V2 — Create Companies Table
-- Normalizes company metadata referenced by the stocks table.
-- Avoids repeating company name, sector, exchange in every stock row.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE companies (
    id          BIGSERIAL       PRIMARY KEY,
    symbol      VARCHAR(20)     NOT NULL UNIQUE,
    name        VARCHAR(255)    NOT NULL,
    sector      VARCHAR(100),
    industry    VARCHAR(150),
    country     VARCHAR(50)     NOT NULL DEFAULT 'USA',
    exchange    VARCHAR(20)     NOT NULL CHECK (exchange IN ('NYSE','NASDAQ','OTHER')),
    market_cap  NUMERIC(20,2),
    description TEXT,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Primary lookup: symbol search and sector filtering
CREATE INDEX idx_companies_symbol   ON companies (symbol);
CREATE INDEX idx_companies_sector   ON companies (sector);
CREATE INDEX idx_companies_exchange ON companies (exchange);

COMMENT ON TABLE  companies          IS 'Master company reference table. Normalized to avoid data duplication in stocks.';
COMMENT ON COLUMN companies.symbol   IS 'Ticker symbol, e.g. AAPL, MSFT (unique, uppercase)';
COMMENT ON COLUMN companies.exchange IS 'NYSE or NASDAQ for US equities';
