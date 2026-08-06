-- ═══════════════════════════════════════════════════════════════════════════
-- V4 — Create Stocks Table
-- Daily OHLCV (Open/High/Low/Close/Volume) records for US equities.
-- References companies for normalized metadata.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE stocks (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    trade_date      DATE            NOT NULL,
    open_price      NUMERIC(15, 4)  NOT NULL CHECK (open_price >= 0),
    high_price      NUMERIC(15, 4)  NOT NULL CHECK (high_price >= 0),
    low_price       NUMERIC(15, 4)  NOT NULL CHECK (low_price >= 0),
    close_price     NUMERIC(15, 4)  NOT NULL CHECK (close_price >= 0),
    adj_close       NUMERIC(15, 4)  CHECK (adj_close >= 0),
    volume          BIGINT          NOT NULL CHECK (volume >= 0),
    daily_return    NUMERIC(10, 6),  -- Calculated: (close - prev_close) / prev_close
    upload_id       BIGINT          REFERENCES upload_history(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Prevents duplicate records for same stock on same date
    CONSTRAINT uq_stocks_company_date UNIQUE (company_id, trade_date),

    -- Data integrity: high must be the actual day high
    CONSTRAINT chk_stocks_high_price CHECK (high_price >= low_price)
);

-- ── Indexes for Analytics Queries ─────────────────────────────────────────
-- Primary analytics join: company + date range queries
CREATE INDEX idx_stocks_company_date  ON stocks (company_id, trade_date DESC);
-- Time-series queries across all stocks
CREATE INDEX idx_stocks_trade_date    ON stocks (trade_date DESC);
-- Top gainers/losers: sort by daily return
CREATE INDEX idx_stocks_daily_return  ON stocks (daily_return DESC NULLS LAST);
-- Volume analysis
CREATE INDEX idx_stocks_volume        ON stocks (volume DESC);
-- Upload traceability
CREATE INDEX idx_stocks_upload_id     ON stocks (upload_id);

COMMENT ON TABLE  stocks              IS 'Daily OHLCV equity data for NYSE/NASDAQ stocks';
COMMENT ON COLUMN stocks.daily_return IS 'Decimal return: 0.025 = 2.5% gain. Populated by ETL or analytics job.';
COMMENT ON COLUMN stocks.adj_close    IS 'Adjusted close accounting for splits and dividends';
