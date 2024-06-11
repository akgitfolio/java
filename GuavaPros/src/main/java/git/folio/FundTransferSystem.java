package git.folio;

import com.google.common.collect.Range;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.graph.MutableValueGraph;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FundTransferSystem {

    private MutableValueGraph<String, Transfer> transferGraph;
    private Map<String, AccountInfo> accounts;

    public FundTransferSystem() {
        this.transferGraph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();
        this.accounts = new HashMap<>();
    }

    public void addAccount(String accountId, AccountType type, double balance) {
        accounts.put(accountId, new AccountInfo(type, balance));
        transferGraph.addNode(accountId);
    }

    public boolean transferFunds(String fromAccount, String toAccount, double amount, Instant timestamp) {
        if (!accounts.containsKey(fromAccount) || !accounts.containsKey(toAccount)) {
            System.out.println("One or both accounts do not exist.");
            return false;
        }

        AccountInfo fromAccountInfo = accounts.get(fromAccount);
        if (fromAccountInfo.balance < amount) {
            System.out.println("Insufficient funds in the source account.");
            return false;
        }

        Range<Double> transferLimit = getTransferLimit(fromAccountInfo.type);
        if (!transferLimit.contains(amount)) {
            System.out.println("Transfer amount exceeds the limit for this account type.");
            return false;
        }

        if (isPotentiallyFraudulent(fromAccount, toAccount, amount)) {
            System.out.println("Potential fraud detected: " + fromAccount + " -> " + toAccount + " ($" + amount + ")");
            return false;
        }

        // Update balances
        fromAccountInfo.balance -= amount;
        accounts.get(toAccount).balance += amount;

        // Record transfer in graph
        Transfer transfer = new Transfer(amount, timestamp);
        transferGraph.putEdgeValue(fromAccount, toAccount, transfer);

        System.out.println("Transfer successful: " + fromAccount + " -> " + toAccount + " ($" + amount + ")");
        return true;
    }

    private Range<Double> getTransferLimit(AccountType type) {
        switch (type) {
            case STANDARD:
                return Range.closed(0.0, 1000.0);
            case PREMIUM:
                return Range.closed(0.0, 5000.0);
            case BUSINESS:
                return Range.closed(0.0, 50000.0);
            default:
                return Range.closed(0.0, 100.0);
        }
    }

    private boolean isPotentiallyFraudulent(String fromAccount, String toAccount, double amount) {
        // Check for cyclic transfers
        if (transferGraph.hasEdgeConnecting(toAccount, fromAccount)) {
            return true;
        }

        // Check for rapid succession of transfers
        long recentTransfers = transferGraph.outDegree(fromAccount);
        if (recentTransfers > 5) {
            return true;
        }

        // Check for unusually large transfers
        AccountInfo accountInfo = accounts.get(fromAccount);
        if (amount > accountInfo.balance * 0.5) {
            return true;
        }

        return false;
    }

    public double getAccountBalance(String accountId) {
        AccountInfo accountInfo = accounts.get(accountId);
        return accountInfo != null ? accountInfo.balance : -1;
    }

    public void printAccountDetails(String accountId) {
        AccountInfo accountInfo = accounts.get(accountId);
        if (accountInfo != null) {
            System.out.println("Account ID: " + accountId);
            System.out.println("Account Type: " + accountInfo.type);
            System.out.println("Balance: $" + accountInfo.balance);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static class AccountInfo {
        AccountType type;
        double balance;

        AccountInfo(AccountType type, double balance) {
            this.type = type;
            this.balance = balance;
        }
    }

    private static class Transfer {
        double amount;
        Instant timestamp;

        Transfer(double amount, Instant timestamp) {
            this.amount = amount;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transfer transfer = (Transfer) o;
            return Double.compare(transfer.amount, amount) == 0 &&
                    Objects.equals(timestamp, transfer.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount, timestamp);
        }
    }

    public enum AccountType {
        STANDARD, PREMIUM, BUSINESS
    }

    public static void main(String[] args) {
        FundTransferSystem system = new FundTransferSystem();

        // Add accounts
        system.addAccount("A001", AccountType.STANDARD, 1000);
        system.addAccount("A002", AccountType.PREMIUM, 5000);
        system.addAccount("B001", AccountType.BUSINESS, 10000);

        // Perform transfers
        system.transferFunds("A001", "A002", 500, Instant.now());
        system.transferFunds("A002", "B001", 2000, Instant.now());
        system.transferFunds("B001", "A001", 1000, Instant.now());

        // Attempt a fraudulent transfer
        system.transferFunds("A001", "A002", 600, Instant.now());

        // Print account details
        system.printAccountDetails("A001");
        system.printAccountDetails("A002");
        system.printAccountDetails("B001");
    }
}