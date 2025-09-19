CREATE TABLE positions (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  symbol VARCHAR(32) NOT NULL,
  net_qty INT NOT NULL DEFAULT 0,
  avg_price NUMERIC(18,4) NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_user_symbol ON positions(user_id, symbol);
