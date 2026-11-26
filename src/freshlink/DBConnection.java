package freshlink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles database connectivity using JDBC.
 */
public class DBConnection {

    // Database configuration credentials
    private static final String URL = "jdbc:mysql://localhost:3306/freshlink_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "dhriti@29"; // Modify this if your MySQL root password is different

    // Establishes and returns a database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
