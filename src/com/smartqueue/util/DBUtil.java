package com.smartqueue.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBUtil.java
 * Utility class for managing JDBC connections to the MySQL database.
 * Uses the Singleton pattern to provide a reusable connection method.
 *
 * Configuration: Update DB_URL, DB_USER, and DB_PASSWORD as per your
 * local MySQL installation.
 */
public class DBUtil {

    // -------------------------------------------------------
    // Database Configuration Constants
    // -------------------------------------------------------
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/smartqueue_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // <-- Change to your MySQL password

    // Static initializer: Load the MySQL JDBC driver once when class is loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j.jar to /WEB-INF/lib/", e);
        }
    }

    /**
     * Returns a new JDBC Connection from the MySQL database.
     * Caller is responsible for closing the connection.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /**
     * Safely closes a JDBC connection (null-safe).
     *
     * @param con The connection to close
     */
    public static void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("DBUtil: Error closing connection - " + e.getMessage());
            }
        }
    }
}
