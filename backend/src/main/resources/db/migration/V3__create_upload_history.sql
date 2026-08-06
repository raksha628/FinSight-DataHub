-- ═══════════════════════════════════════════════════════════════════════════
-- V3 — Create Upload History Table
-- Tracks every CSV file ingested into the platform, including ETL results
-- and validation reports. Must be created BEFORE asset tables (stocks, etc.)
-- since they reference this table.
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE upload_history (
    id                  BIGSERIAL       PRIMARY KEY,
    filename            VARCHAR(255)    NOT NULL,
    original_filename   VARCHAR(255)    NOT NULL,
    asset_type          VARCHAR(30)     NOT NULL
                                        CHECK (asset_type IN ('STOCK','ETF','MUTUAL_FUND','CRYPTO','FOREX','SECTOR_PERFORMANCE')),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PROCESSING'
                                        CHECK (status IN ('PROCESSING','SUCCESS','FAILED','PARTIAL')),
    total_rows          INT             DEFAULT 0,
    accepted_rows       INT             DEFAULT 0,
    rejected_rows       INT             DEFAULT 0,
    file_size_bytes     BIGINT,
    uploaded_by         BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    processed_at        TIMESTAMPTZ,
    processing_ms       BIGINT,         -- Total ETL processing time in milliseconds
    validation_report   TEXT,           -- JSON: list of rejected rows with reasons
    error_message       TEXT            -- Top-level error if entire upload failed
);

CREATE INDEX idx_upload_history_status       ON upload_history (status);
CREATE INDEX idx_upload_history_asset_type   ON upload_history (asset_type);
CREATE INDEX idx_upload_history_uploaded_by  ON upload_history (uploaded_by);
CREATE INDEX idx_upload_history_uploaded_at  ON upload_history (uploaded_at DESC);

COMMENT ON TABLE  upload_history                  IS 'ETL pipeline audit trail: every CSV ingestion event with outcome and validation summary';
COMMENT ON COLUMN upload_history.validation_report IS 'JSON array of rejected records: [{row, reason, values}]';
COMMENT ON COLUMN upload_history.processing_ms     IS 'Wall-clock ETL duration for performance monitoring';
