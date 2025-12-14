package configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    //  connection parameters
    private static final String URL = "jdbc:mariadb://localhost:3306/PatronManagerDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Establishes and returns a connection to the MariaDB database.
     * @return A valid Connection object.
     * @throws SQLException If a connection error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
        //  The DriverManager uses the JDBC driver (MariaDB Connector/J)
        //  to establish the connection using the URL, User, and Password.
    }

    // --- CONNECTION TESTER ---
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        Connection testConn = null;
        try {
            System.out.println("Attempting to connect to the database...");
            
            // 1. Get the connection
            testConn = getConnection();
            
            // 2. Check the status
            if (testConn != null && !testConn.isClosed()) {
                System.out.println("\n******************************************");
                System.out.println(" ✅ SUCCESS! Database connection established!");
                System.out.println("******************************************\n");
            } else {
                System.out.println("FAILURE: Connection object is null or closed.");
            }

        } catch (SQLException e) {
            System.err.println("\n❌ CONNECTION ERROR! Please check the following:");
            System.err.println("1. Is XAMPP's MariaDB/MySQL running?");
            System.err.println("2. Is the database name in the URL ('PatronManagerDB') correct?");
            System.err.println("3. Is the MariaDB/MySQL JDBC driver (JAR file) correctly added to the classpath (settings.json)?");
            System.err.println("--- Full Error Details ---");
            e.printStackTrace();
        } finally {
            // 3. Close the connection in a finally block
            if (testConn != null) {
                try {
                    testConn.close();
                    System.out.println("\nConnection closed.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}