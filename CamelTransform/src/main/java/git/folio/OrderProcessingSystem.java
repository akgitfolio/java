package git.folio;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;

public class OrderProcessingSystem {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Global error handler
                onException(Exception.class)
                        .handled(true)
                        .log("Error processing order: ${exception.message}")
                        .process(exchange -> {
                            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            System.err.println("Exception caught: " + cause.getMessage());
                            cause.printStackTrace();
                        });

                // REST API Input Route
                restConfiguration().component("netty-http").host("localhost").port(8060);

                rest("/api")
                        .post("/order")
                        .consumes("application/json")
                        .type(Order.class)
                        .to("direct:unmarshalOrder");

                from("direct:unmarshalOrder")
                        .log("Received JSON: ${body}")
                        .unmarshal().json(JsonLibrary.Jackson, Order.class)
                        .log("Unmarshalled to Order: ${body}")
                        .to("direct:processOrder");

                // Common Processing Route
                from("direct:processOrder")
                        .log("Processing order: ${body}")
                        .process(exchange -> {
                            Order order = exchange.getIn().getBody(Order.class);
                            if (order == null) {
                                throw new IllegalArgumentException("Order object is null");
                            }
                            log.info("Order details: id={}, customerName={}, total={}", order.getId(), order.getCustomerName(), order.getTotal());
                            if (order.getTotal() > 100) {
                                order.setTotal(order.getTotal() * 0.9); // 10% discount
                            }
                        })
                        .choice()
                        .when(simple("${body.total} > 1000"))
                        .to("direct:highValueOrder")
                        .otherwise()
                        .to("direct:regularOrder");

                // High Value Order Route
                from("direct:highValueOrder")
                        .log("High value order received: ${body}")
                        .marshal().json(JsonLibrary.Jackson)
                        .to("file:output/high-value?fileName=${date:now:yyyyMMdd-HHmmss}.json");

                // Regular Order Route
                from("direct:regularOrder")
                        .log("Regular order received: ${body}")
                        .marshal().json(JsonLibrary.Jackson)
                        .to("file:output/regular?fileName=${date:now:yyyyMMdd-HHmmss}.json");
            }
        });

        context.start();
        System.out.println("Order Processing System started. Press Ctrl+C to stop.");
        Thread.sleep(Long.MAX_VALUE); // Run indefinitely
    }
    public static class Order {
        private int id;
        private String customerName;
        private double total;

        public Order() {} // Default constructor

        public Order(int id, String customerName, double total) {
            this.id = id;
            this.customerName = customerName;
            this.total = total;
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }

        @Override
        public String toString() {
            return "Order{id=" + id + ", customerName='" + customerName + "', total=" + total + "}";
        }
    }
}