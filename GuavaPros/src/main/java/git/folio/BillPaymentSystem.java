package git.folio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

public class BillPaymentSystem {

    private final Multimap<String, Bill> userBills;
    private final EventBus eventBus;

    public BillPaymentSystem() {
        this.userBills = ArrayListMultimap.create();
        this.eventBus = new EventBus();
    }

    public void addBill(String userId, Bill bill) {
        userBills.put(userId, bill);
    }

    public Collection<Bill> getUserBills(String userId) {
        return userBills.get(userId);
    }

    public void registerForNotifications(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void checkUpcomingPayments() {
        LocalDate today = LocalDate.now();
        for (String userId : userBills.keySet()) {
            for (Bill bill : userBills.get(userId)) {
                if (bill.getDueDate().minusDays(3).equals(today)) {
                    eventBus.post(new PaymentDueEvent(userId, bill));
                }
            }
        }
    }

    public static class Bill {
        private final String id;
        private final double amount;
        private final LocalDate dueDate;

        public Bill(double amount, LocalDate dueDate) {
            this.id = UUID.randomUUID().toString();
            this.amount = amount;
            this.dueDate = dueDate;
        }

        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }
    }

    public static class PaymentDueEvent {
        private final String userId;
        private final Bill bill;

        public PaymentDueEvent(String userId, Bill bill) {
            this.userId = userId;
            this.bill = bill;
        }

        public String getUserId() {
            return userId;
        }

        public Bill getBill() {
            return bill;
        }
    }

    public static class NotificationService {
        @Subscribe
        public void onPaymentDue(PaymentDueEvent event) {
            System.out.println("Payment due notification for user " + event.getUserId() +
                    ": Bill " + event.getBill().getId() +
                    " amount $" + event.getBill().getAmount() +
                    " due on " + event.getBill().getDueDate());
        }
    }

    public static void main(String[] args) {
        BillPaymentSystem system = new BillPaymentSystem();

        // Register notification service
        NotificationService notificationService = new NotificationService();
        system.registerForNotifications(notificationService);

        // Add bills for users
        system.addBill("user1", new Bill(100.0, LocalDate.now().plusDays(3)));
        system.addBill("user1", new Bill(50.0, LocalDate.now().plusDays(5)));
        system.addBill("user2", new Bill(75.0, LocalDate.now().plusDays(3)));

        // Check for upcoming payments
        system.checkUpcomingPayments();

        // Display all bills for a user
        System.out.println("Bills for user1:");
        for (Bill bill : system.getUserBills("user1")) {
            System.out.println("Bill ID: " + bill.getId() + ", Amount: $" + bill.getAmount() + ", Due Date: " + bill.getDueDate());
        }
    }
}