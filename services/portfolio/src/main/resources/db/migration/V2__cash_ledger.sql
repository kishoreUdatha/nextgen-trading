CREATE TABLE cash_ledger(
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  delta NUMERIC(18,2) NOT NULL,
  reason VARCHAR(64) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Helper view for current cash
CREATE OR REPLACE VIEW wallet AS
  SELECT user_id, COALESCE(SUM(delta),0) AS balance FROM cash_ledger GROUP BY user_id;

-- Seed demo user with 1,000,000
INSERT INTO cash_ledger(user_id, delta, reason) VALUES ('demo-user', 1000000.00, 'SEED');
