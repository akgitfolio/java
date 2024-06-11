package git.folio;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PasswordSecurityManager {

    private static final int EXPECTED_INSERTIONS = 10_000_000; // Adjust based on your dataset
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;
    private final BloomFilter<String> compromisedPasswords;

    public PasswordSecurityManager() {
        compromisedPasswords = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                EXPECTED_INSERTIONS,
                FALSE_POSITIVE_PROBABILITY
        );
        loadCompromisedPasswords();
    }

    private void loadCompromisedPasswords() {
        try {
            List<String> passwords = Files.readLines(new File("compromised_passwords.txt"), StandardCharsets.UTF_8);
            for (String password : passwords) {
                compromisedPasswords.put(password);
            }
        } catch (IOException e) {
            System.err.println("Error loading compromised passwords: " + e.getMessage());
        }
    }

    public boolean isPasswordCompromised(String password) {
        return compromisedPasswords.mightContain(password);
    }

    public String hashPassword(String password) {
        return Hashing.sha256()
                .hashString(password, StandardCharsets.UTF_8)
                .toString();
    }

    public boolean verifyPassword(String inputPassword, String storedHash) {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(storedHash);
    }

    public boolean isPasswordSecure(String password) {
        if (password.length() < 8) {
            return false;
        }
        if (isPasswordCompromised(password)) {
            return false;
        }
        // Add more password strength checks here
        return true;
    }

    public static void main(String[] args) {
        PasswordSecurityManager manager = new PasswordSecurityManager();

        // Example usage
        String password = "mySecurePassword123";

        if (manager.isPasswordSecure(password)) {
            String hashedPassword = manager.hashPassword(password);
            System.out.println("Password is secure. Hashed value: " + hashedPassword);

            // Simulating password verification
            boolean verified = manager.verifyPassword(password, hashedPassword);
            System.out.println("Password verification: " + (verified ? "Successful" : "Failed"));
        } else {
            System.out.println("Password is not secure. Please choose a different password.");
        }
    }
}