package git.folio;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransactionProcessor {

    private final LoadingCache<String, BigDecimal> accountBalanceCache;
    private final ListeningExecutorService executorService;
    private final List<Transaction> transactionHistory;

    public TransactionProcessor() {
        // Initialize LoadingCache for account balances
        accountBalanceCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(new CacheLoader<String, BigDecimal>() {
                    @Override
                    public BigDecimal load(String accountId) {
                        // Simulating database fetch
                        return fetchAccountBalanceFromDB(accountId);
                    }
                });

        // Initialize ListeningExecutorService for async processing
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

        transactionHistory = new ArrayList<>();
    }

    public BigDecimal getAccountBalance(String accountId) throws Exception {
        return accountBalanceCache.get(accountId);
    }

    public ListenableFuture<Boolean> processTransaction(Transaction transaction) {
        return executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Simulating transaction processing
                Thread.sleep(1000); // Simulating some processing time

                BigDecimal currentBalance = getAccountBalance(transaction.getAccountId());
                BigDecimal newBalance = currentBalance.add(transaction.getAmount());

                // Update cache with new balance
                accountBalanceCache.put(transaction.getAccountId(), newBalance);

                // Add to transaction history
                transactionHistory.add(transaction);

                return true;
            }
        });
    }

    public List<Transaction> getTransactionHistory() {
        // Use Guava's Ordering to sort transactions by timestamp
        Ordering<Transaction> byTimestamp = new Ordering<Transaction>() {
            @Override
            public int compare(Transaction left, Transaction right) {
                return left.getTimestamp().compareTo(right.getTimestamp());
            }
        };

        return byTimestamp.sortedCopy(transactionHistory);
    }

    private BigDecimal fetchAccountBalanceFromDB(String accountId) {
        // Simulating database fetch
        return new BigDecimal("1000.00");
    }

    public static void main(String[] args) throws Exception {
        TransactionProcessor processor = new TransactionProcessor();

        // Example usage
        String accountId = "12345";
        System.out.println("Initial balance: " + processor.getAccountBalance(accountId));

        Transaction transaction1 = new Transaction(accountId, new BigDecimal("100.00"));
        Transaction transaction2 = new Transaction(accountId, new BigDecimal("-50.00"));

        ListenableFuture<Boolean> future1 = processor.processTransaction(transaction1);
        ListenableFuture<Boolean> future2 = processor.processTransaction(transaction2);

        // Wait for transactions to complete
        future1.get();
        future2.get();

        System.out.println("Final balance: " + processor.getAccountBalance(accountId));

        System.out.println("Transaction History:");
        for (Transaction t : processor.getTransactionHistory()) {
            System.out.println(t);
        }

        processor.executorService.shutdown();
    }
}

class Transaction {
    private String accountId;
    private BigDecimal amount;
    private Long timestamp;

    public Transaction(String accountId, BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}