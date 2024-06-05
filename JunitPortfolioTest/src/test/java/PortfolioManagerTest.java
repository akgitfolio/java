import git.folio.PortfolioManager;
import org.junit.jupiter.api.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioManagerTest {

    private PortfolioManager portfolioManager;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        portfolioManager = new PortfolioManager(10000.0);
    }

    @Test
    @DisplayName("Test initial cash balance")
    void testInitialCashBalance() {
        assertEquals(10000.0, portfolioManager.getCash(), DELTA);
    }

    @Test
    @DisplayName("Test buying stock successfully")
    void testBuyStockSuccess() {
        assertTrue(portfolioManager.buyStock("AAPL", 10, 150.0));
        assertEquals(8500.0, portfolioManager.getCash(), DELTA);
        assertEquals(10.0, portfolioManager.getPortfolio().get("AAPL"), DELTA);
    }

    @Test
    @DisplayName("Test buying stock with insufficient funds")
    void testBuyStockInsufficientFunds() {
        assertFalse(portfolioManager.buyStock("GOOGL", 100, 2000.0));
        assertEquals(10000.0, portfolioManager.getCash(), DELTA);
        assertFalse(portfolioManager.getPortfolio().containsKey("GOOGL"));
    }

    @Test
    @DisplayName("Test selling stock successfully")
    void testSellStockSuccess() {
        portfolioManager.buyStock("MSFT", 20, 200.0);
        assertTrue(portfolioManager.sellStock("MSFT", 10, 220.0));
        assertEquals(8200.0, portfolioManager.getCash(), DELTA);
        assertEquals(10.0, portfolioManager.getPortfolio().get("MSFT"), DELTA);
    }

    @Test
    @DisplayName("Test selling stock with insufficient quantity")
    void testSellStockInsufficientQuantity() {
        portfolioManager.buyStock("TSLA", 5, 600.0);
        assertFalse(portfolioManager.sellStock("TSLA", 10, 650.0));
        assertEquals(7000.0, portfolioManager.getCash(), DELTA);
        assertEquals(5.0, portfolioManager.getPortfolio().get("TSLA"), DELTA);
    }

    @Test
    @DisplayName("Test selling non-existent stock")
    void testSellNonExistentStock() {
        assertFalse(portfolioManager.sellStock("AMZN", 5, 3000.0));
        assertEquals(10000.0, portfolioManager.getCash(), DELTA);
    }

    @Test
    @DisplayName("Test calculating portfolio value")
    void testCalculatePortfolioValue() {
        assertTrue(portfolioManager.buyStock("AAPL", 10, 150.0));
        assertFalse(portfolioManager.buyStock("GOOGL", 5, 2000.0)); // This should fail due to insufficient funds

        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("AAPL", 160.0);
        currentPrices.put("GOOGL", 2100.0);

        double expectedValue = 8500.0 + (10 * 160.0); // Cash + AAPL value
        assertEquals(expectedValue, portfolioManager.calculatePortfolioValue(currentPrices), DELTA);
    }

    @Test
    @DisplayName("Test portfolio value with missing price data")
    void testCalculatePortfolioValueMissingData() {
        portfolioManager.buyStock("AAPL", 10, 150.0);
        portfolioManager.buyStock("GOOGL", 5, 2000.0);

        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("AAPL", 160.0);

        double expectedValue = 8500.0 + (10 * 160.0);
        assertEquals(expectedValue, portfolioManager.calculatePortfolioValue(currentPrices), DELTA);
    }

    @Test
    @DisplayName("Test multiple buy and sell operations")
    void testMultipleBuyAndSellOperations() {
        double initialCash = 10000.0; // Assuming this is the initial cash value
        double expectedCash = initialCash;

        assertTrue(portfolioManager.buyStock("AAPL", 10, 150.0));
        expectedCash -= 10 * 150.0;

        assertFalse(portfolioManager.buyStock("GOOGL", 5, 2000.0)); // This should fail due to insufficient funds

        assertTrue(portfolioManager.sellStock("AAPL", 5, 160.0));
        expectedCash += 5 * 160.0;

        assertFalse(portfolioManager.buyStock("MSFT", 15, 200.0)); // This should fail due to insufficient funds

        assertEquals(expectedCash, portfolioManager.getCash(), DELTA);
        assertEquals(5.0, portfolioManager.getPortfolio().get("AAPL"), DELTA);
        assertFalse(portfolioManager.getPortfolio().containsKey("GOOGL"));
        assertFalse(portfolioManager.getPortfolio().containsKey("MSFT"));
    }

    @Test
    @DisplayName("Test selling all shares of a stock")
    void testSellingAllShares() {
        portfolioManager.buyStock("AAPL", 10, 150.0);
        assertTrue(portfolioManager.sellStock("AAPL", 10, 160.0));
        assertEquals(10100.0, portfolioManager.getCash(), DELTA);
        assertFalse(portfolioManager.getPortfolio().containsKey("AAPL"));
    }
}