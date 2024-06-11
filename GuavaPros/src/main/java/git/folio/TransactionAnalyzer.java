package git.folio;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.FluentIterable;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

public class TransactionAnalyzer {

    public static class Transaction {
        private String id;
        private double amount;
        private String category;
        private LocalDate date;

        public Transaction(String id, double amount, String category, LocalDate date) {
            this.id = id;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }

        // Getters
        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    private List<Transaction> transactions;

    
    public TransactionAnalyzer() {
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public Multiset<String> analyzeCategoryFrequency() {
        Multiset<String> categoryFrequency = HashMultiset.create();
        for (Transaction transaction : transactions) {
            categoryFrequency.add(transaction.getCategory());
        }
        return categoryFrequency;
    }

    public List<Transaction> getHighValueTransactions(final double threshold) {
        return FluentIterable.from(transactions)
                .filter(new Predicate<Transaction>() {
                    @Override
                    public boolean apply(Transaction transaction) {
                        return transaction.getAmount() > threshold;
                    }
                })
                .toList();
    }

    public List<Transaction> getTransactionsInDateRange(final LocalDate startDate, final LocalDate endDate) {
        return FluentIterable.from(transactions)
                .filter(new Predicate<Transaction>() {
                    @Override
                    public boolean apply(Transaction transaction) {
                        return !transaction.getDate().isBefore(startDate) && !transaction.getDate().isAfter(endDate);
                    }
                })
                .toList();
    }

    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();

        // Add sample transactions
        analyzer.addTransaction(new Transaction("1", 100.0, "Food", LocalDate.of(2023, 6, 1)));
        analyzer.addTransaction(new Transaction("2", 500.0, "Electronics", LocalDate.of(2023, 6, 2)));
        analyzer.addTransaction(new Transaction("3", 50.0, "Food", LocalDate.of(2023, 6, 3)));
        analyzer.addTransaction(new Transaction("4", 1000.0, "Travel", LocalDate.of(2023, 6, 4)));
        analyzer.addTransaction(new Transaction("5", 200.0, "Food", LocalDate.of(2023, 6, 5)));

        // Analyze category frequency
        Multiset<String> categoryFrequency = analyzer.analyzeCategoryFrequency();
        System.out.println("Category Frequency:");
        for (Multiset.Entry<String> entry : categoryFrequency.entrySet()) {
            System.out.println(entry.getElement() + ": " + entry.getCount());
        }

        // Get high value transactions
        List<Transaction> highValueTransactions = analyzer.getHighValueTransactions(300.0);
        System.out.println("\nHigh Value Transactions (>$300):");
        for (Transaction transaction : highValueTransactions) {
            System.out.println(transaction.getId() + ": $" + transaction.getAmount());
        }

        // Get transactions in date range
        LocalDate startDate = LocalDate.of(2023, 6, 2);
        LocalDate endDate = LocalDate.of(2023, 6, 4);
        List<Transaction> transactionsInRange = analyzer.getTransactionsInDateRange(startDate, endDate);
        System.out.println("\nTransactions between " + startDate + " and " + endDate + ":");
        for (Transaction transaction : transactionsInRange) {
            System.out.println(transaction.getId() + ": " + transaction.getDate());
        }
    }
}