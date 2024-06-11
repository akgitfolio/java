package git.folio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class UserAuthService {

    private final EventBus eventBus;
    private final ImmutableSet<String> validRoles;

    public UserAuthService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.validRoles = ImmutableSet.of("USER", "ADMIN", "MANAGER");
        eventBus.register(this);
    }

    public void authenticateUser(String username, String password) {
        Preconditions.checkNotNull(username, "Username cannot be null");
        Preconditions.checkNotNull(password, "Password cannot be null");
        Preconditions.checkArgument(!username.isEmpty(), "Username cannot be empty");
        Preconditions.checkArgument(password.length() >= 8, "Password must be at least 8 characters");

        // Perform authentication logic here

        // If authentication successful, trigger OTP generation
        eventBus.post(new OtpGenerationEvent(username));
    }

    @Subscribe
    public void handleOtpGeneration(OtpGenerationEvent event) {
        // Generate OTP and send to user
        String otp = generateOtp();
        sendOtpToUser(event.getUsername(), otp);
    }

    public boolean verifyOtp(String username, String otp) {
        Preconditions.checkNotNull(username, "Username cannot be null");
        Preconditions.checkNotNull(otp, "OTP cannot be null");

        // Verify OTP logic here
        return true; // Placeholder
    }

    public ImmutableSet<String> getUserRoles(String username) {
        Preconditions.checkNotNull(username, "Username cannot be null");

        // Fetch user roles from database
        ImmutableSet<String> userRoles = ImmutableSet.of("USER", "MANAGER");

        Preconditions.checkArgument(validRoles.containsAll(userRoles),
                "User has invalid role(s)");

        return userRoles;
    }

    public boolean hasPermission(String username, String permission) {
        ImmutableSet<String> userRoles = getUserRoles(username);

        // Check if any of the user's roles grant the required permission
        return userRoles.contains("ADMIN") ||
                (permission.equals("READ") && userRoles.contains("USER")) ||
                (permission.equals("WRITE") && userRoles.contains("MANAGER"));
    }

    private String generateOtp() {
        // OTP generation logic
        return "123456"; // Placeholder
    }

    private void sendOtpToUser(String username, String otp) {
        // Logic to send OTP to user
    }

    private static class OtpGenerationEvent {
        private final String username;

        public OtpGenerationEvent(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        UserAuthService authService = new UserAuthService(eventBus);

        // Demonstrate user authentication
        try {
            authService.authenticateUser("johndoe", "password123");
            System.out.println("User authentication initiated for johndoe");
        } catch (IllegalArgumentException e) {
            System.out.println("Authentication failed: " + e.getMessage());
        }

        // Demonstrate OTP verification
        boolean otpVerified = authService.verifyOtp("johndoe", "123456");
        System.out.println("OTP verification result: " + (otpVerified ? "Successful" : "Failed"));

        // Demonstrate role retrieval
        try {
            System.out.println("User roles for johndoe: " + authService.getUserRoles("johndoe"));
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to retrieve roles: " + e.getMessage());
        }

        // Demonstrate permission checking
        String username = "johndoe";
        String[] permissions = {"READ", "WRITE", "DELETE"};
        for (String permission : permissions) {
            boolean hasPermission = authService.hasPermission(username, permission);
            System.out.println("Does " + username + " have " + permission + " permission? " + hasPermission);
        }

        // Demonstrate error handling
        try {
            authService.authenticateUser(null, "password123");
        } catch (NullPointerException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            authService.authenticateUser("janedoe", "short");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}