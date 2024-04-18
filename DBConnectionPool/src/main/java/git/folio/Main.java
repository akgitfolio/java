package git.folio;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DBConnectionPool pool = null;
        Connection conn = null;

        try {
            pool = new DBConnectionPool("jdbc:mysql://localhost:3306/mydb", "user", "password", 10);

            conn = pool.getConnection();
            // Use the connection...
            System.out.println("Successfully connected to the database.");

            // When done, release the connection back to the pool
            pool.releaseConnection(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Ensure resources are properly closed
            if (conn != null) {
                try {
                    pool.releaseConnection(conn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // When shutting down your application
            if (pool != null) {
                try {
                    pool.shutdown();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}