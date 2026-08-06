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

-- ═══════════════════════════════════════════════════════════════════════════
-- V7 — Create Crypto Table
-- Cryptocurrency daily OHLCV with market cap tracking.
-- Uses higher precision (8 decimal places) for crypto prices.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE crypto (
    id              BIGSERIAL       PRIMARY KEY,
    symbol          VARCHAR(20)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    trade_date      DATE            NOT NULL,
    open_price      NUMERIC(20, 8)  NOT NULL CHECK (open_price >= 0),
    high_price      NUMERIC(20, 8)  NOT NULL CHECK (high_price >= 0),
    low_price       NUMERIC(20, 8)  NOT NULL CHECK (low_price >= 0),
    close_price     NUMERIC(20, 8)  NOT NULL CHECK (close_price >= 0),
    volume          NUMERIC(30, 4)  CHECK (volume >= 0),
    market_cap      NUMERIC(30, 2)  CHECK (market_cap >= 0),
    daily_return    NUMERIC(10, 6),
    upload_id       BIGINT          REFERENCES upload_history(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_crypto_symbol_date UNIQUE (symbol, trade_date)
);

CREATE INDEX idx_crypto_symbol     ON crypto (symbol);
CREATE INDEX idx_crypto_trade_date ON crypto (trade_date DESC);
CREATE INDEX idx_crypto_market_cap ON crypto (market_cap DESC NULLS LAST);
