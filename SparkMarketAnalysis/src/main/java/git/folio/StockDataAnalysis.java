package git.folio;

import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.types.*;
import static org.apache.spark.sql.functions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StockDataAnalysis {

    public static void main(String[] args) {
        // Initialize Spark session
        SparkSession spark = SparkSession.builder()
                .appName("Stock Data Analysis")
                .master("local[*]")
                .getOrCreate();

        // Generate synthetic stock data
        Dataset<Row> stockData = generateSyntheticStockData(spark);

        // Save the data
        stockData.write().mode("overwrite").parquet("stock_data.parquet");

        // Read the data back
        Dataset<Row> loadedData = spark.read().parquet("stock_data.parquet");

        // Perform some basic analysis
        performAnalysis(loadedData);

        spark.stop();
    }

    private static Dataset<Row> generateSyntheticStockData(SparkSession spark) {
        List<Row> data = new ArrayList<>();
        Random random = new Random();
        String[] companies = {"AAPL", "GOOGL", "MSFT", "AMZN"};

        for (String company : companies) {
            double price = 100.0; // Starting price
            for (int day = 1; day <= 365; day++) {
                double change = (random.nextDouble() - 0.5) * 5; // Random price change
                price += change;
                if (price < 0) price = 0; // Ensure price doesn't go negative
                data.add(RowFactory.create(company, day, price));
            }
        }

        StructType schema = new StructType(new StructField[]{
                new StructField("company", DataTypes.StringType, false, Metadata.empty()),
                new StructField("day", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("price", DataTypes.DoubleType, false, Metadata.empty())
        });

        return spark.createDataFrame(data, schema);
    }

    private static void performAnalysis(Dataset<Row> data) {
        // Calculate average price for each company
        System.out.println("Average price for each company:");
        data.groupBy("company")
                .agg(avg("price").alias("avg_price"))
                .show();

        // Find the highest price for each company
        System.out.println("Highest price for each company:");
        data.groupBy("company")
                .agg(max("price").alias("max_price"))
                .show();

        // Calculate daily price change
        Dataset<Row> withChange = data.withColumn("price_change",
                        col("price").minus(lag("price", 1).over(Window.partitionBy("company").orderBy("day"))))
                .na().drop(); // Remove first row where price_change is null

        // Find days with biggest price changes
        System.out.println("Top 5 days with biggest price changes:");
        withChange.orderBy(abs(col("price_change")).desc())
                .select("company", "day", "price_change")
                .show(5);

        // Calculate 7-day moving average
        Dataset<Row> withMovingAvg = data.withColumn("moving_avg",
                avg("price").over(Window.partitionBy("company").orderBy("day").rowsBetween(-6, 0)));

        System.out.println("Sample of data with 7-day moving average:");
        withMovingAvg.show(10);
    }
}