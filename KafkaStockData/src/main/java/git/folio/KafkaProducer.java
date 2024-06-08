package git.folio;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class KafkaProducer {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topic = "stock-data-topic";

        // Create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the Producer
        try (org.apache.kafka.clients.producer.KafkaProducer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(properties)) {
            Random random = new Random();
            String[] stockSymbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "FB"};

            while (true) {
                for (String symbol : stockSymbols) {
                    String stockData = generateStockData(symbol, random);
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, symbol, stockData);

                    // Send data
                    producer.send(record, (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println("Stock data sent successfully. Topic: " + metadata.topic() +
                                    ", Partition: " + metadata.partition() + ", Offset: " + metadata.offset());
                        } else {
                            System.err.println("Error sending stock data: " + exception.getMessage());
                        }
                    });
                }

                // Flush the producer
                producer.flush();

                // Sleep for 5 seconds before generating next batch of data
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String generateStockData(String symbol, Random random) {
        double basePrice = 100.0; // Base price for simplicity
        double high = basePrice + random.nextDouble() * 10;
        double low = basePrice - random.nextDouble() * 10;
        double close = low + (high - low) * random.nextDouble();
        int volume = random.nextInt(1000000) + 100000; // Random volume between 100,000 and 1,100,000
        boolean up = close > basePrice;
        double mid = (high + low) / 2;
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return String.format("%s,%.2f,%.2f,%.2f,%d,%b,%.2f,%s",
                symbol, high, low, close, volume, up, mid, timestamp);
    }
}