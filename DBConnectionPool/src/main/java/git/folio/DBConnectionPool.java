package git.folio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBConnectionPool {

    private String url;
    private String user;
    private String password;
    private int maxConnections;
    private List<Connection> DBConnectionPool;
    private List<Connection> usedConnections = new ArrayList<>();

    public DBConnectionPool(String url, String user, String password, int maxConnections) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxConnections = maxConnections;
        this.DBConnectionPool = new ArrayList<>(maxConnections);
        for (int i = 0; i < maxConnections; i++) {
            DBConnectionPool.add(createConnection());
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        if (DBConnectionPool.isEmpty()) {
            if (usedConnections.size() < maxConnections) {
                DBConnectionPool.add(createConnection());
            } else {
                throw new SQLException("Maximum number of connections reached");
            }
        }

        Connection connection = DBConnectionPool.remove(DBConnectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        DBConnectionPool.add(connection);
        return usedConnections.remove(connection);
    }

    public int getSize() {
        return DBConnectionPool.size() + usedConnections.size();
    }

    public void shutdown() throws SQLException {
        usedConnections.forEach(this::releaseConnection);
        for (Connection c : DBConnectionPool) {
            c.close();
        }
        DBConnectionPool.clear();
    }

    // Getters
    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxConnections() {
        return maxConnections;
    }
}