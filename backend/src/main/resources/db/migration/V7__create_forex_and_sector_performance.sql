-- ═══════════════════════════════════════════════════════════════════════════
-- V7 — Create Forex Table
-- Currency pair daily exchange rates.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE forex (
    id              BIGSERIAL       PRIMARY KEY,
    base_currency   CHAR(3)         NOT NULL,   -- e.g. USD
    quote_currency  CHAR(3)         NOT NULL,   -- e.g. EUR
    trade_date      DATE            NOT NULL,
    open_rate       NUMERIC(15, 6)  NOT NULL CHECK (open_rate > 0),
    high_rate       NUMERIC(15, 6)  NOT NULL CHECK (high_rate > 0),
    low_rate        NUMERIC(15, 6)  NOT NULL CHECK (low_rate > 0),
    close_rate      NUMERIC(15, 6)  NOT NULL CHECK (close_rate > 0),
    daily_change    NUMERIC(10, 6),
    upload_id       BIGINT          REFERENCES upload_history(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_forex_pair_date UNIQUE (base_currency, quote_currency, trade_date)
);

CREATE INDEX idx_forex_pair       ON forex (base_currency, quote_currency);
CREATE INDEX idx_forex_trade_date ON forex (trade_date DESC);

-- ═══════════════════════════════════════════════════════════════════════════
-- V8 — Create Sector Performance Table
-- Aggregated sector-level daily/weekly/monthly performance metrics.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE sector_performance (
    id                      BIGSERIAL       PRIMARY KEY,
    sector_name             VARCHAR(100)    NOT NULL,
    performance_date        DATE            NOT NULL,
    daily_return_pct        NUMERIC(8, 4),
    weekly_return_pct       NUMERIC(8, 4),
    monthly_return_pct      NUMERIC(8, 4),
    ytd_return_pct          NUMERIC(8, 4),
    total_market_cap        NUMERIC(30, 2)  CHECK (total_market_cap >= 0),
    total_volume            BIGINT          CHECK (total_volume >= 0),
    advancing_count         INT             DEFAULT 0,
    declining_count         INT             DEFAULT 0,
    unchanged_count         INT             DEFAULT 0,
    upload_id               BIGINT          REFERENCES upload_history(id) ON DELETE SET NULL,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_sector_date UNIQUE (sector_name, performance_date)
);

CREATE INDEX idx_sector_performance_sector ON sector_performance (sector_name);
CREATE INDEX idx_sector_performance_date   ON sector_performance (performance_date DESC);
CREATE INDEX idx_sector_performance_return ON sector_performance (daily_return_pct DESC NULLS LAST);

COMMENT ON TABLE sector_performance IS 'Pre-aggregated sector metrics. Populated by ETL and analytics scheduler.';
