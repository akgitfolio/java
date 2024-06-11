package git.folio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.util.concurrent.RateLimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AccountManagementSystem {

    private final BiMap<String, String> accountToUserIdMap;
    private final Map<String, String> userCredentials;
    private final RateLimiter loginRateLimiter;

    public AccountManagementSystem() {
        this.accountToUserIdMap = HashBiMap.create();
        this.userCredentials = new HashMap<>();
        // Allow 3 login attempts per minute
        this.loginRateLimiter = RateLimiter.create(3.0 / 60.0);
    }

    public void createAccount(String userId, String accountNumber, String password) {
        if (accountToUserIdMap.containsKey(accountNumber) || accountToUserIdMap.containsValue(userId)) {
            throw new IllegalArgumentException("Account number or User ID already exists");
        }
        accountToUserIdMap.put(accountNumber, userId);
        userCredentials.put(userId, password);
        System.out.println("Account created successfully for user: " + userId);
    }

    public boolean login(String accountNumber, String password) {
        if (!loginRateLimiter.tryAcquire(1, 1, TimeUnit.SECONDS)) {
            System.out.println("Too many login attempts. Please try again later.");
            return false;
        }

        String userId = accountToUserIdMap.get(accountNumber);
        if (userId == null) {
            System.out.println("Account not found");
            return false;
        }

        if (userCredentials.get(userId).equals(password)) {
            System.out.println("Login successful for user: " + userId);
            return true;
        } else {
            System.out.println("Incorrect password");
            return false;
        }
    }

    public String getUserIdByAccountNumber(String accountNumber) {
        return accountToUserIdMap.get(accountNumber);
    }

    public String getAccountNumberByUserId(String userId) {
        return accountToUserIdMap.inverse().get(userId);
    }

    public static void main(String[] args) {
        AccountManagementSystem ams = new AccountManagementSystem();

        // Create some accounts
        ams.createAccount("user1", "ACC001", "password1");
        ams.createAccount("user2", "ACC002", "password2");

        // Demonstrate BiMap functionality
        System.out.println("User ID for ACC001: " + ams.getUserIdByAccountNumber("ACC001"));
        System.out.println("Account number for user2: " + ams.getAccountNumberByUserId("user2"));

        // Demonstrate login with rate limiting
        for (int i = 0; i < 5; i++) {
            ams.login("ACC001", "password1");
        }

        // Wait for a minute to reset the rate limiter
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Try login again
        ams.login("ACC001", "password1");
    }
}