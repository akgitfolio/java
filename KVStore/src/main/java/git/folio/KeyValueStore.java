package git.folio;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class KeyValueStore {
    private Map<String, String> inMemoryStore;
    private Properties persistentStore;
    private String filePath;
    private boolean isPersistent;

    public KeyValueStore(boolean isPersistent, String filePath) {
        this.isPersistent = isPersistent;
        if (isPersistent) {
            this.filePath = filePath;
            this.persistentStore = new Properties();
            loadProperties();
        } else {
            this.inMemoryStore = new HashMap<>();
        }
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            persistentStore.load(fis);
        } catch (IOException e) {
            System.out.println("Error loading properties: " + e.getMessage());
        }
    }

    public void put(String key, String value) {
        if (isPersistent) {
            persistentStore.setProperty(key, value);
            saveProperties();
        } else {
            inMemoryStore.put(key, value);
        }
    }

    public String get(String key) {
        if (isPersistent) {
            return persistentStore.getProperty(key);
        } else {
            return inMemoryStore.get(key);
        }
    }

    public void delete(String key) {
        if (isPersistent) {
            persistentStore.remove(key);
            saveProperties();
        } else {
            inMemoryStore.remove(key);
        }
    }

    private void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            persistentStore.store(fos, null);
        } catch (IOException e) {
            System.out.println("Error saving properties: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        KeyValueStore store;

        System.out.println("Choose store type:");
        System.out.println("1. In-memory store");
        System.out.println("2. Persistent store");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (choice == 1) {
            store = new KeyValueStore(false, null);
            System.out.println("In-memory store initialized.");
        } else if (choice == 2) {
            System.out.print("Enter file path for persistent store: ");
            String filePath = scanner.nextLine();
            store = new KeyValueStore(true, filePath);
            System.out.println("Persistent store initialized.");
        } else {
            System.out.println("Invalid choice. Exiting.");
            return;
        }

        while (true) {
            System.out.println("\nChoose operation:");
            System.out.println("1. Put");
            System.out.println("2. Get");
            System.out.println("3. Delete");
            System.out.println("4. Exit");
            int operation = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (operation) {
                case 1:
                    System.out.print("Enter key: ");
                    String key = scanner.nextLine();
                    System.out.print("Enter value: ");
                    String value = scanner.nextLine();
                    store.put(key, value);
                    System.out.println("Value stored.");
                    break;
                case 2:
                    System.out.print("Enter key: ");
                    key = scanner.nextLine();
                    value = store.get(key);
                    if (value != null) {
                        System.out.println("Value: " + value);
                    } else {
                        System.out.println("Key not found.");
                    }
                    break;
                case 3:
                    System.out.print("Enter key: ");
                    key = scanner.nextLine();
                    store.delete(key);
                    System.out.println("Key deleted.");
                    break;
                case 4:
                    System.out.println("Exiting.");
                    return;
                default:
                    System.out.println("Invalid operation.");
            }
        }
    }
}