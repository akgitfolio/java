package git.folio;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

public class WebScraper {

    public static void main(String[] args) {
        String url = "https://books.toscrape.com/catalogue/category/books_1/index.html";
        String outputFile = "scraped_books.csv";

        try {
            // Connect to the website and get the HTML document
            Document doc = Jsoup.connect(url).get();

            // Create a FileWriter object to write data to a CSV file
            FileWriter writer = new FileWriter(outputFile);

            // Write CSV header
            writer.write("Title,Price,Availability,Rating\n");

            // Extract data using CSS selectors
            Elements books = doc.select("article.product_pod");

            for (Element book : books) {
                String title = book.select("h3 a").attr("title");
                String price = book.select("div.product_price p.price_color").text();
                String availability = book.select("div.product_price p.instock.availability").text();
                String rating = book.select("p.star-rating").attr("class").replace("star-rating ", "");

                // Write data to CSV file
                writer.write(escapeSpecialCharacters(title) + "," +
                        escapeSpecialCharacters(price) + "," +
                        escapeSpecialCharacters(availability) + "," +
                        escapeSpecialCharacters(rating) + "\n");
            }

            writer.close();
            System.out.println("Web scraping completed. Data saved to " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to escape special characters in CSV
    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}