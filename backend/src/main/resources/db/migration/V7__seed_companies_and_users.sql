-- ═══════════════════════════════════════════════════════════════════════════
-- V9 — Seed Data: US Companies and Admin User
--
-- Seeds 50 US companies across 11 S&P 500 sectors (NYSE/NASDAQ).
-- Also seeds a default admin user for first-time setup.
--
-- Admin login: admin@finsight.com / Admin@123
-- (BCrypt hash of 'Admin@123', strength 12)
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Default Admin User ─────────────────────────────────────────────────────
INSERT INTO users (username, email, password_hash, role, is_active)
VALUES (
    'admin',
    'admin@finsight.com',
    '$2a$12$gJUhcn5a1XRY8p5Z6A9xMeeqjd7Yo.8k2FsCK.j9BIBSe0I9e.hKS',
    'ADMIN',
    true
);

-- ── Analyst Demo User ──────────────────────────────────────────────────────
-- Password: Analyst@123
INSERT INTO users (username, email, password_hash, role, is_active)
VALUES (
    'analyst',
    'analyst@finsight.com',
    '$2a$12$OW8a3hLHVf6m62nJlAFdRuGzrqNKBqJJQmMmUyLhHxPFWKzZBGg4m',
    'ANALYST',
    true
);

-- ── US Companies — Technology ──────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('AAPL',  'Apple Inc.',                       'Technology', 'Consumer Electronics',       'USA', 'NASDAQ', 2900000000000),
('MSFT',  'Microsoft Corporation',            'Technology', 'Software - Infrastructure',  'USA', 'NASDAQ', 3100000000000),
('NVDA',  'NVIDIA Corporation',               'Technology', 'Semiconductors',             'USA', 'NASDAQ', 2400000000000),
('AMD',   'Advanced Micro Devices, Inc.',     'Technology', 'Semiconductors',             'USA', 'NASDAQ',  280000000000),
('INTC',  'Intel Corporation',               'Technology', 'Semiconductors',             'USA', 'NASDAQ',   95000000000),
('ORCL',  'Oracle Corporation',              'Technology', 'Software - Infrastructure',  'USA', 'NYSE',    440000000000),
('CRM',   'Salesforce, Inc.',                'Technology', 'Software - Application',     'USA', 'NYSE',    290000000000),
('ADBE',  'Adobe Inc.',                      'Technology', 'Software - Application',     'USA', 'NASDAQ',  240000000000),
('QCOM',  'Qualcomm Incorporated',           'Technology', 'Semiconductors',             'USA', 'NASDAQ',  170000000000),
('NOW',   'ServiceNow, Inc.',                'Technology', 'Software - Application',     'USA', 'NYSE',    180000000000);

-- ── Communication Services ─────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('GOOGL', 'Alphabet Inc.',                   'Communication Services', 'Internet Content & Information', 'USA', 'NASDAQ', 2000000000000),
('META',  'Meta Platforms, Inc.',            'Communication Services', 'Internet Content & Information', 'USA', 'NASDAQ', 1300000000000),
('NFLX',  'Netflix, Inc.',                   'Communication Services', 'Entertainment',                  'USA', 'NASDAQ',  290000000000),
('DIS',   'The Walt Disney Company',         'Communication Services', 'Entertainment',                  'USA', 'NYSE',    195000000000),
('CMCSA', 'Comcast Corporation',             'Communication Services', 'Telecom Services',               'USA', 'NASDAQ',  180000000000);

-- ── Consumer Discretionary ────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('AMZN',  'Amazon.com, Inc.',               'Consumer Discretionary', 'Internet Retail',           'USA', 'NASDAQ', 1950000000000),
('TSLA',  'Tesla, Inc.',                    'Consumer Discretionary', 'Auto Manufacturers',        'USA', 'NASDAQ',  700000000000),
('NKE',   'Nike, Inc.',                     'Consumer Discretionary', 'Footwear & Accessories',    'USA', 'NYSE',    150000000000),
('MCD',   'McDonald''s Corporation',        'Consumer Discretionary', 'Restaurants',               'USA', 'NYSE',    210000000000),
('SBUX',  'Starbucks Corporation',          'Consumer Discretionary', 'Restaurants',               'USA', 'NASDAQ',  105000000000);

-- ── Financials ────────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('JPM',   'JPMorgan Chase & Co.',           'Financials', 'Banks - Diversified',        'USA', 'NYSE',    590000000000),
('BAC',   'Bank of America Corporation',   'Financials', 'Banks - Diversified',        'USA', 'NYSE',    290000000000),
('WFC',   'Wells Fargo & Company',         'Financials', 'Banks - Diversified',        'USA', 'NYSE',    210000000000),
('GS',    'The Goldman Sachs Group, Inc.', 'Financials', 'Capital Markets',            'USA', 'NYSE',    160000000000),
('MS',    'Morgan Stanley',               'Financials', 'Capital Markets',            'USA', 'NYSE',    155000000000),
('BRK-B', 'Berkshire Hathaway Inc.',       'Financials', 'Insurance - Diversified',   'USA', 'NYSE',    875000000000),
('V',     'Visa Inc.',                     'Financials', 'Credit Services',            'USA', 'NYSE',    535000000000),
('MA',    'Mastercard Incorporated',       'Financials', 'Credit Services',            'USA', 'NYSE',    440000000000);

-- ── Healthcare ────────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('JNJ',   'Johnson & Johnson',             'Healthcare', 'Drug Manufacturers - General', 'USA', 'NYSE',   385000000000),
('UNH',   'UnitedHealth Group Incorporated','Healthcare', 'Healthcare Plans',            'USA', 'NYSE',   450000000000),
('PFE',   'Pfizer Inc.',                   'Healthcare', 'Drug Manufacturers - General', 'USA', 'NYSE',   165000000000),
('ABBV',  'AbbVie Inc.',                   'Healthcare', 'Drug Manufacturers - General', 'USA', 'NYSE',   280000000000),
('MRK',   'Merck & Co., Inc.',             'Healthcare', 'Drug Manufacturers - General', 'USA', 'NYSE',   260000000000);

-- ── Energy ────────────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('XOM',   'Exxon Mobil Corporation',       'Energy', 'Oil & Gas Integrated',      'USA', 'NYSE',   480000000000),
('CVX',   'Chevron Corporation',           'Energy', 'Oil & Gas Integrated',      'USA', 'NYSE',   290000000000),
('COP',   'ConocoPhillips',               'Energy', 'Oil & Gas E&P',             'USA', 'NYSE',   140000000000),
('SLB',   'SLB (Schlumberger)',            'Energy', 'Oil & Gas Equipment',       'USA', 'NYSE',    65000000000);

-- ── Industrials ───────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('CAT',   'Caterpillar Inc.',             'Industrials', 'Farm & Heavy Construction',  'USA', 'NYSE',   180000000000),
('HON',   'Honeywell International Inc.', 'Industrials', 'Conglomerates',              'USA', 'NASDAQ', 135000000000),
('BA',    'The Boeing Company',           'Industrials', 'Aerospace & Defense',         'USA', 'NYSE',   110000000000),
('GE',    'GE Aerospace',                 'Industrials', 'Aerospace & Defense',         'USA', 'NYSE',   165000000000),
('UPS',   'United Parcel Service, Inc.',  'Industrials', 'Integrated Freight & Logistics','USA','NYSE', 100000000000);

-- ── Consumer Staples ──────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('PG',    'The Procter & Gamble Company', 'Consumer Staples', 'Household & Personal Products', 'USA', 'NYSE',   360000000000),
('KO',    'The Coca-Cola Company',        'Consumer Staples', 'Beverages - Non-Alcoholic',     'USA', 'NYSE',   265000000000),
('PEP',   'PepsiCo, Inc.',               'Consumer Staples', 'Beverages - Non-Alcoholic',     'USA', 'NASDAQ', 220000000000),
('WMT',   'Walmart Inc.',               'Consumer Staples', 'Discount Stores',               'USA', 'NYSE',   600000000000),
('COST',  'Costco Wholesale Corporation','Consumer Staples', 'Discount Stores',               'USA', 'NASDAQ', 380000000000);

-- ── Real Estate ───────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('AMT',   'American Tower Corporation',   'Real Estate', 'REIT - Specialty',        'USA', 'NYSE',    90000000000),
('PLD',   'Prologis, Inc.',              'Real Estate', 'REIT - Industrial',       'USA', 'NYSE',   110000000000),
('EQIX',  'Equinix, Inc.',              'Real Estate', 'REIT - Specialty',        'USA', 'NASDAQ',  75000000000);

-- ── Materials ─────────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('LIN',   'Linde plc',                  'Materials', 'Specialty Chemicals',      'USA', 'NASDAQ', 220000000000),
('APD',   'Air Products and Chemicals', 'Materials', 'Specialty Chemicals',      'USA', 'NYSE',    55000000000),
('ECL',   'Ecolab Inc.',               'Materials', 'Specialty Chemicals',      'USA', 'NYSE',    57000000000);

-- ── Utilities ─────────────────────────────────────────────────────────────
INSERT INTO companies (symbol, name, sector, industry, country, exchange, market_cap) VALUES
('NEE',   'NextEra Energy, Inc.',       'Utilities', 'Utilities - Regulated Electric', 'USA', 'NYSE',  145000000000),
('DUK',   'Duke Energy Corporation',   'Utilities', 'Utilities - Regulated Electric', 'USA', 'NYSE',   80000000000),
('SO',    'The Southern Company',      'Utilities', 'Utilities - Regulated Electric', 'USA', 'NYSE',   75000000000);
