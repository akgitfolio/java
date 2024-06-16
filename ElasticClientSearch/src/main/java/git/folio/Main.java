package git.folio;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ClientService clientService = new ClientService();

        try {
            // Save a new client
            Client newClient = new Client();
            newClient.setName("John Doe");
            newClient.setEmail("john.doe@example.com");
            newClient.setAccountType("Premium");
            newClient.setInvestmentAmount(100000.0);
            newClient.setRegistrationDate(new Date());

            String clientId = clientService.saveClient(newClient);
            System.out.println("New client saved with ID: " + clientId);

            // Search for clients
            List<Client> searchResults = clientService.searchClients("John");
            System.out.println("Search results:");
            for (Client client : searchResults) {
                System.out.println(client.getName() + " - " + client.getEmail());
            }

            // Find clients by account type
            List<Client> premiumClients = clientService.findByAccountType("Premium");
            System.out.println("Premium clients:");
            for (Client client : premiumClients) {
                System.out.println(client.getName() + " - " + client.getAccountType());
            }

            // Find clients by investment amount
            List<Client> highInvestors = clientService.findByInvestmentAmountGreaterThan(50000.0);
            System.out.println("High investors:");
            for (Client client : highInvestors) {
                System.out.println(client.getName() + " - $" + client.getInvestmentAmount());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ElasticsearchConfig.closeClient();
        }
    }
}