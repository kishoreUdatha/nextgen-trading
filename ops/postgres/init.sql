-- ==============================
-- Create Schemas
-- ==============================
CREATE SCHEMA IF NOT EXISTS oms;
CREATE SCHEMA IF NOT EXISTS risk;
CREATE SCHEMA IF NOT EXISTS portfolio;
CREATE SCHEMA IF NOT EXISTS marketdata;
CREATE SCHEMA IF NOT EXISTS exec_adapter;

-- ==============================
-- OMS Enums
-- ==============================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
CREATE TYPE oms.order_status AS ENUM ('NEW','VALIDATED','REJECTED','ROUTED','PARTIALLY_FILLED','FILLED','CANCELLED','EXPIRED');
END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_type') THEN
CREATE TYPE oms.order_type AS ENUM ('MARKET','LIMIT','SL','SL_M');
END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tif') THEN
CREATE TYPE oms.tif AS ENUM ('DAY','IOC','GTC');
END IF;
END$$;

-- ==============================
-- OMS Tables
-- ==============================
CREATE TABLE IF NOT EXISTS oms.orders (
                                          id UUID PRIMARY KEY,
                                          client_order_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    segment VARCHAR(32) NOT NULL,
    side VARCHAR(8) NOT NULL,
    qty INT NOT NULL,
    filled_qty INT NOT NULL DEFAULT 0,
    price NUMERIC(18,4),
    trigger_price NUMERIC(18,4),
    order_type oms.order_type NOT NULL,
    tif oms.tif NOT NULL,
    status oms.order_status NOT NULL,
    exchange_order_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version INT NOT NULL DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS oms.order_events (
                                                id UUID PRIMARY KEY,
                                                order_id UUID NOT NULL REFERENCES oms.orders(id),
    prev_status oms.order_status,
    new_status oms.order_status NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
    );

CREATE TABLE IF NOT EXISTS oms.idempotency_records (
                                                       key VARCHAR(64) PRIMARY KEY,
    request_hash VARCHAR(128) NOT NULL,
    response_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
    );

CREATE TABLE IF NOT EXISTS oms.event_outbox (
                                                id UUID PRIMARY KEY,
                                                aggregate_id UUID NOT NULL,
                                                topic VARCHAR(200) NOT NULL,
    type VARCHAR(100) NOT NULL,
    payload_json TEXT NOT NULL,
    headers_json TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ
    );

-- ==============================
-- OMS Indexes
-- ==============================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='oms' AND indexname='idx_orders_symbol_status') THEN
CREATE INDEX idx_orders_symbol_status ON oms.orders(symbol, status);
END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname='oms' AND indexname='idx_outbox_published') THEN
CREATE INDEX idx_outbox_published ON oms.event_outbox(published_at);
END IF;
END$$;
