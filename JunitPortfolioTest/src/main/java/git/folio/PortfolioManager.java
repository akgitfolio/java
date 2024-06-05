package git.folio;

import java.util.HashMap;
import java.util.Map;

public class PortfolioManager {
    private Map<String, Double> portfolio;
    private double cash;

    public PortfolioManager(double initialCash) {
        this.portfolio = new HashMap<>();
        this.cash = initialCash;
    }

    public boolean buyStock(String ticker, int quantity, double price) {
        double totalCost = quantity * price;
        if (totalCost > cash) {
            return false;
        }

        portfolio.put(ticker, portfolio.getOrDefault(ticker, 0.0) + quantity);
        cash -= totalCost;
        return true;
    }

    public boolean sellStock(String ticker, int quantity, double price) {
        if (!portfolio.containsKey(ticker) || portfolio.get(ticker) < quantity) {
            return false;
        }

        portfolio.put(ticker, portfolio.get(ticker) - quantity);
        cash += quantity * price;

        if (portfolio.get(ticker) == 0) {
            portfolio.remove(ticker);
        }
        return true;
    }

    public double calculatePortfolioValue(Map<String, Double> currentPrices) {
        double totalValue = cash;
        for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
            String ticker = entry.getKey();
            double quantity = entry.getValue();
            double currentPrice = currentPrices.getOrDefault(ticker, 0.0);
            totalValue += quantity * currentPrice;
        }
        return totalValue;
    }

    public double getCash() {
        return cash;
    }

    public Map<String, Double> getPortfolio() {
        return new HashMap<>(portfolio);
    }
}