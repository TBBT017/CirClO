package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections to the circlo_db MySQL database.
 *
 * Configuration:
 *   Host    : localhost:3306
 *   Database: circlo_db
 *   User    : root
 *   Password: 123456789  (change here and in README if your setup differs)
 *
 * The static initializer eagerly loads the MySQL JDBC driver so that any
 * ClassNotFoundException surfaces at startup rather than at first use.
 */
public class DBConnection {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/circlo_db" +
            "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "123456789";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Add mysql-connector-j to the classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Open and return a new JDBC Connection.
     * Callers are responsible for closing the connection (use try-with-resources).
     *
     * @return a live Connection object
     * @throws SQLException if the database is unreachable or credentials are wrong
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Safely close a Connection, suppressing any exception that occurs during close.
     *
     * @param conn the Connection to close (may be null)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("DBConnection.closeConnection error: " + e.getMessage());
            }
        }
    }
}
