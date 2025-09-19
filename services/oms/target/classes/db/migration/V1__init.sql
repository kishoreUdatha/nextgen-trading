CREATE TYPE order_status AS ENUM ('NEW','VALIDATED','REJECTED','ROUTED','PARTIALLY_FILLED','FILLED','CANCELLED','EXPIRED');
CREATE TYPE order_type AS ENUM ('MARKET','LIMIT','SL','SL_M');
CREATE TYPE tif AS ENUM ('DAY','IOC','GTC');

CREATE TABLE orders (
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
  order_type order_type NOT NULL,
  tif tif NOT NULL,
  status order_status NOT NULL,
  exchange_order_id VARCHAR(64),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  version INT NOT NULL DEFAULT 0
);

CREATE TABLE order_events (
  id UUID PRIMARY KEY,
  order_id UUID NOT NULL REFERENCES orders(id),
  prev_status order_status,
  new_status order_status NOT NULL,
  payload_json TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE idempotency_records (
  key VARCHAR(64) PRIMARY KEY,
  request_hash VARCHAR(128) NOT NULL,
  response_json TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE event_outbox (
  id UUID PRIMARY KEY,
  aggregate_id UUID NOT NULL,
  topic VARCHAR(200) NOT NULL,
  type VARCHAR(100) NOT NULL,
  payload_json TEXT NOT NULL,
  headers_json TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_orders_symbol_status ON orders(symbol, status);
CREATE INDEX idx_outbox_published ON event_outbox(published_at);
