-- ═══════════════════════════════════════════════════════════════════════════
-- V8 — Create Market Snapshots Table
-- Daily market-level summary. One row per trading day.
-- Populated by the AI market summary scheduler.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE market_snapshots (
    id                  BIGSERIAL       PRIMARY KEY,
    snapshot_date       DATE            NOT NULL UNIQUE,
    total_stocks        INT             DEFAULT 0,
    advancing_count     INT             DEFAULT 0,
    declining_count     INT             DEFAULT 0,
    unchanged_count     INT             DEFAULT 0,
    total_volume        BIGINT          DEFAULT 0,
    total_market_cap    NUMERIC(30, 2),
    top_gainer_symbol   VARCHAR(20),
    top_gainer_return   NUMERIC(8, 4),
    top_loser_symbol    VARCHAR(20),
    top_loser_return    NUMERIC(8, 4),
    market_sentiment    VARCHAR(20)     CHECK (market_sentiment IN ('BULLISH','BEARISH','NEUTRAL')),
    generated_summary   TEXT,           -- AI-generated market narrative
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_market_snapshots_date ON market_snapshots (snapshot_date DESC);

COMMENT ON TABLE  market_snapshots                IS 'Daily market-level aggregates and AI-generated summaries. One row per trading day.';
COMMENT ON COLUMN market_snapshots.generated_summary IS 'Gemini AI-generated paragraph summarizing the trading day';

-- ═══════════════════════════════════════════════════════════════════════════
-- V9 — Create Saved Queries Table
-- Stores AI natural-language query history per user.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE saved_queries (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    natural_language    TEXT            NOT NULL,
    generated_sql       TEXT            NOT NULL,
    is_successful       BOOLEAN         NOT NULL DEFAULT TRUE,
    row_count           INT             DEFAULT 0,
    execution_ms        INT             DEFAULT 0,
    error_message       TEXT,
    executed_at         TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_saved_queries_user_id      ON saved_queries (user_id);
CREATE INDEX idx_saved_queries_executed_at  ON saved_queries (executed_at DESC);
CREATE INDEX idx_saved_queries_successful   ON saved_queries (is_successful);

-- ═══════════════════════════════════════════════════════════════════════════
-- V10 — Create Audit Logs Table
-- Compliance-level audit trail for all significant user actions.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE audit_logs (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          REFERENCES users(id) ON DELETE SET NULL,  -- NULL for system events
    action      VARCHAR(100)    NOT NULL,  -- e.g. LOGIN, LOGOUT, UPLOAD, AI_QUERY, REPORT_DOWNLOAD
    entity_type VARCHAR(50),               -- e.g. USER, STOCK, UPLOAD
    entity_id   BIGINT,
    details     JSONB,                     -- Structured context data
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id    ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action     ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);
-- GIN index for JSONB detail searches
CREATE INDEX idx_audit_logs_details    ON audit_logs USING GIN (details);

COMMENT ON TABLE  audit_logs         IS 'Immutable compliance audit trail. Rows are never updated or deleted.';
COMMENT ON COLUMN audit_logs.details IS 'JSONB context: request params, affected row counts, etc.';
