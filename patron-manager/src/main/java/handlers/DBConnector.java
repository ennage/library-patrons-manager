package handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    //  connection parameters
    private static final String URL = "jdbc:mariadb://localhost:3306/PatronRecordsDB";
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
}