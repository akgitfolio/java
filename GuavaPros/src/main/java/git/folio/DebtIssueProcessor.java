package git.folio;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.RateLimiter;

public class DebtIssueProcessor {

    private final RateLimiter rateLimiter;

    public DebtIssueProcessor(double issuesPerSecond) {
        this.rateLimiter = RateLimiter.create(issuesPerSecond);
    }

    public static class DebtIssue {
        private String debtorName;
        private Double debtAmount;
        private Integer creditScore;

        public DebtIssue(String debtorName, Double debtAmount, Integer creditScore) {
            this.debtorName = debtorName;
            this.debtAmount = debtAmount;
            this.creditScore = creditScore;
        }

        // Getters
        public String getDebtorName() { return debtorName; }
        public Double getDebtAmount() { return debtAmount; }
        public Integer getCreditScore() { return creditScore; }
    }

    public boolean processDebtIssue(DebtIssue issue) {
        // Wait for permission from RateLimiter
        rateLimiter.acquire();

        Optional<String> name = Optional.fromNullable(issue.getDebtorName());
        Optional<Double> amount = Optional.fromNullable(issue.getDebtAmount());
        Optional<Integer> creditScore = Optional.fromNullable(issue.getCreditScore());

        if (!name.isPresent()) {
            System.out.println("Debt issue rejected: Debtor name is missing");
            return false;
        }

        if (!amount.isPresent()) {
            System.out.println("Debt issue rejected: Debt amount is missing");
            return false;
        }

        if (!creditScore.isPresent()) {
            System.out.println("Debt issue rejected: Credit score is missing");
            return false;
        }

        // Process the debt issue
        boolean resolved = evaluateDebtIssue(name.get(), amount.get(), creditScore.get());

        if (resolved) {
            System.out.println("Debt issue resolved for " + name.get() + " with debt amount $" + amount.get());
        } else {
            System.out.println("Debt issue unresolved for " + name.get());
        }

        return resolved;
    }

    private boolean evaluateDebtIssue(String name, double amount, int creditScore) {
        // Simple evaluation logic
        return creditScore >= 600 && amount <= 75000;
    }

    public static void main(String[] args) {
        DebtIssueProcessor processor = new DebtIssueProcessor(2.0); // 2 issues per second

        // Test cases
        DebtIssue issue1 = new DebtIssue("John Doe", 30000.0, 650);
        DebtIssue issue2 = new DebtIssue("Jane Smith", null, 700);
        DebtIssue issue3 = new DebtIssue("Bob Johnson", 80000.0, 550);
        DebtIssue issue4 = new DebtIssue(null, 40000.0, 620);

        processor.processDebtIssue(issue1);
        processor.processDebtIssue(issue2);
        processor.processDebtIssue(issue3);
        processor.processDebtIssue(issue4);
    }
}