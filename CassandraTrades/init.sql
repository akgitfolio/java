CREATE KEYSPACE IF NOT EXISTS trades_keyspace
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};

USE trades_keyspace;

CREATE TABLE trades (
    trade_id UUID,
    symbol TEXT,
    trade_date DATE,
    trade_time TIME,
    price DECIMAL,
    quantity INT,
    trade_type TEXT,
    trader_id UUID,
    PRIMARY KEY ((symbol, trade_date), trade_time, trade_id)
) WITH CLUSTERING ORDER BY (trade_time DESC);

CREATE MATERIALIZED VIEW trades_by_trader AS
SELECT *
FROM trades
WHERE trader_id IS NOT NULL AND symbol IS NOT NULL AND trade_date IS NOT NULL AND trade_time IS NOT NULL AND trade_id IS NOT NULL
PRIMARY KEY ((trader_id, trade_date), symbol, trade_time, trade_id)
WITH CLUSTERING ORDER BY (symbol ASC, trade_time DESC);