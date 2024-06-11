package git.folio;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PortfolioManager {

    private final Table<String, String, Double> portfolioTable;
    private final LoadingCache<String, Double> stockPriceCache;

    public PortfolioManager() {
        // Initialize the portfolio table
        portfolioTable = HashBasedTable.create();

        // Initialize the stock price cache
        stockPriceCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Cache entries expire after 5 minutes
                .build(new CacheLoader<String, Double>() {
                    @Override
                    public Double load(String ticker) {
                        return fetchRealTimeStockPrice(ticker);
                    }
                });
    }

    // Add an investment to the portfolio
    public void addInvestment(String investor, String stockTicker, double shares) {
        portfolioTable.put(investor, stockTicker, shares);
    }

    // Get an investor's portfolio
    public Map<String, Double> getInvestorPortfolio(String investor) {
        return portfolioTable.row(investor);
    }

    // Get all investments for a particular stock
    public Map<String, Double> getStockInvestments(String stockTicker) {
        return portfolioTable.column(stockTicker);
    }

    // Calculate the total value of an investor's portfolio
    public double calculatePortfolioValue(String investor) throws Exception {
        double totalValue = 0.0;
        Map<String, Double> portfolio = getInvestorPortfolio(investor);

        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            String stockTicker = entry.getKey();
            double shares = entry.getValue();
            double price = stockPriceCache.get(stockTicker);
            totalValue += shares * price;
        }

        return totalValue;
    }

    // Simulated method to fetch real-time stock price
    private double fetchRealTimeStockPrice(String ticker) {
        // In a real implementation, this would call an external API
        // For demonstration, we'll return a random price between 50 and 200
        return 50 + Math.random() * 150;
    }

    public static void main(String[] args) throws Exception {
        PortfolioManager manager = new PortfolioManager();

        // Add some sample investments
        manager.addInvestment("Alice", "AAPL", 10);
        manager.addInvestment("Alice", "GOOGL", 5);
        manager.addInvestment("Bob", "AAPL", 15);
        manager.addInvestment("Bob", "MSFT", 20);

        // Print Alice's portfolio
        System.out.println("Alice's portfolio: " + manager.getInvestorPortfolio("Alice"));

        // Print all investments in AAPL
        System.out.println("All AAPL investments: " + manager.getStockInvestments("AAPL"));

        // Calculate and print the value of Bob's portfolio
        System.out.println("Bob's portfolio value: $" + String.format("%.2f", manager.calculatePortfolioValue("Bob")));
    }
}