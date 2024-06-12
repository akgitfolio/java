package git.folio;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class TradesDataStore implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TradesDataStore.class);
    private final CqlSession session;

    public TradesDataStore(String host, int port) {
        this.session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter("datacenter1")
                .build();
        initializeSchema();
    }

    private void initializeSchema() {
        try {
            session.execute("CREATE KEYSPACE IF NOT EXISTS trades_keyspace " +
                    "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3}");
            session.execute("USE trades_keyspace");
            session.execute("CREATE TABLE IF NOT EXISTS trades (" +
                    "trade_id UUID, " +
                    "symbol TEXT, " +
                    "trade_date DATE, " +
                    "trade_time TIME, " +
                    "price DECIMAL, " +
                    "quantity INT, " +
                    "trade_type TEXT, " +
                    "trader_id UUID, " +
                    "PRIMARY KEY ((symbol, trade_date), trade_time, trade_id)" +
                    ") WITH CLUSTERING ORDER BY (trade_time DESC)");
            session.execute("CREATE TABLE IF NOT EXISTS trades_by_trader (" +
                    "trader_id UUID, " +
                    "trade_date DATE, " +
                    "symbol TEXT, " +
                    "trade_time TIME, " +
                    "trade_id UUID, " +
                    "price DECIMAL, " +
                    "quantity INT, " +
                    "trade_type TEXT, " +
                    "PRIMARY KEY ((trader_id, trade_date), symbol, trade_time, trade_id)" +
                    ") WITH CLUSTERING ORDER BY (symbol ASC, trade_time DESC)");
        } catch (Exception e) {
            logger.error("Failed to initialize schema", e);
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }

    public void insertTrade(UUID tradeId, String symbol, LocalDate tradeDate, LocalTime tradeTime,
                            BigDecimal price, int quantity, String tradeType, UUID traderId) {
        try {
            PreparedStatement stmt1 = session.prepare(
                    "INSERT INTO trades (trade_id, symbol, trade_date, trade_time, price, quantity, trade_type, trader_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            session.execute(stmt1.bind(tradeId, symbol, tradeDate, tradeTime, price, quantity, tradeType, traderId));

            PreparedStatement stmt2 = session.prepare(
                    "INSERT INTO trades_by_trader (trader_id, trade_date, symbol, trade_time, trade_id, price, quantity, trade_type) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            session.execute(stmt2.bind(traderId, tradeDate, symbol, tradeTime, tradeId, price, quantity, tradeType));
        } catch (Exception e) {
            logger.error("Failed to insert trade", e);
            throw new RuntimeException("Failed to insert trade", e);
        }
    }

    public ResultSet getTradesBySymbolAndDate(String symbol, LocalDate date) {
        try {
            PreparedStatement stmt = session.prepare(
                    "SELECT * FROM trades WHERE symbol = ? AND trade_date = ?");
            return session.execute(stmt.bind(symbol, date));
        } catch (Exception e) {
            logger.error("Failed to get trades by symbol and date", e);
            throw new RuntimeException("Failed to get trades by symbol and date", e);
        }
    }

    public ResultSet getTradesByTraderAndDate(UUID traderId, LocalDate date) {
        try {
            PreparedStatement stmt = session.prepare(
                    "SELECT * FROM trades_by_trader WHERE trader_id = ? AND trade_date = ?");
            return session.execute(stmt.bind(traderId, date));
        } catch (Exception e) {
            logger.error("Failed to get trades by trader and date", e);
            throw new RuntimeException("Failed to get trades by trader and date", e);
        }
    }

    @Override
    public void close() {
        if (session != null && !session.isClosed()) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Failed to close session", e);
            }
        }
    }

    public static void main(String[] args) {
        try (TradesDataStore dataStore = new TradesDataStore("127.0.0.1", 9042)) {
            // Insert sample trades
            UUID trader1 = UUID.randomUUID();
            UUID trader2 = UUID.randomUUID();

            dataStore.insertTrade(UUID.randomUUID(), "AAPL", LocalDate.now(), LocalTime.now(), new BigDecimal("150.50"), 100, "BUY", trader1);
            dataStore.insertTrade(UUID.randomUUID(), "GOOGL", LocalDate.now(), LocalTime.now(), new BigDecimal("2800.75"), 50, "SELL", trader2);
            dataStore.insertTrade(UUID.randomUUID(), "AAPL", LocalDate.now(), LocalTime.now(), new BigDecimal("151.00"), 200, "BUY", trader2);

            // Query trades by symbol and date
            System.out.println("Trades for AAPL today:");
            ResultSet resultSet = dataStore.getTradesBySymbolAndDate("AAPL", LocalDate.now());
            for (Row row : resultSet) {
                System.out.println("Trade ID: " + row.getUuid("trade_id") +
                        ", Symbol: " + row.getString("symbol") +
                        ", Price: " + row.getBigDecimal("price") +
                        ", Quantity: " + row.getInt("quantity") +
                        ", Trader ID: " + row.getUuid("trader_id"));
            }

            // Query trades by trader and date
            System.out.println("\nTrades for Trader 2 today:");
            resultSet = dataStore.getTradesByTraderAndDate(trader2, LocalDate.now());
            for (Row row : resultSet) {
                System.out.println("Trade ID: " + row.getUuid("trade_id") +
                        ", Symbol: " + row.getString("symbol") +
                        ", Price: " + row.getBigDecimal("price") +
                        ", Quantity: " + row.getInt("quantity") +
                        ", Trade Type: " + row.getString("trade_type"));
            }

            // Wait for user input to prevent program from exiting
            System.out.println("Press Enter to exit...");
            System.in.read();
        } catch (Exception e) {
            logger.error("An error occurred", e);
        }
    }

}