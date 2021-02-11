CREATE TYPE trade.trade_status as ENUM ('SUBMITTED', 'CANCELLED', 'COMPLETED', 'FAILED');
CREATE TYPE trade.trade_side as ENUM ('BUY', 'SELL');

CREATE CAST (character varying as trade.trade_status) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying as trade.trade_side) WITH INOUT AS IMPLICIT;

CREATE TABLE IF NOT EXISTS trade.trade
(
  trade_uuid UUID NOT NULL DEFAULT uuid_generate_v4(),
  account_uuid UUID,
  symbol TEXT NOT NULL,
  quantity INTEGER NOT NULL,
  status trade.trade_status NOT NULL,
  side trade.trade_side NOT NULL,
  price numeric(15,2) NOT NULL,
  created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  created_by TEXT,
  updated_by TEXT,
  PRIMARY KEY(trade_uuid),
  FOREIGN KEY (account_uuid) REFERENCES trade.account (account_uuid)
);