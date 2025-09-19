CREATE TABLE IF NOT EXISTS trades_analytics
(
  orderId String,
  userId String,
  symbol String,
  segment LowCardinality(String),
  side LowCardinality(String),
  qty Int32,
  price Decimal(18,4),
  notional Decimal(22,4) MATERIALIZED qty * price,
  ts DateTime DEFAULT now()
) ENGINE = MergeTree
PARTITION BY toDate(ts)
ORDER BY (symbol, ts);

CREATE TABLE IF NOT EXISTS pnl_minute
(
  userId String,
  symbol String,
  netQty Int32,
  avgPrice Decimal(18,4),
  lastPx Decimal(18,4),
  unrealized Decimal(22,4) MATERIALIZED (toDecimal64(netQty,4) * (lastPx - avgPrice)),
  windowStart DateTime,
  windowEnd   DateTime
) ENGINE = MergeTree
PARTITION BY toDate(windowStart)
ORDER BY (userId, symbol, windowStart);

CREATE TABLE IF NOT EXISTS md_ticks
(
  symbol String,
  ltp Decimal(18,4),
  ts DateTime
) ENGINE = MergeTree
PARTITION BY toDate(ts)
ORDER BY (symbol, ts);
