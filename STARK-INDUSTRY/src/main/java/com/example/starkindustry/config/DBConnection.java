package com.example.starkindustry.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton: only one DBConnection object exists in the whole application.
 * Everyone gets the connection through DBConnection.getInstance().getConnection().
 */
public class DBConnection {

    // ====== CHANGE THESE TO MATCH YOUR MYSQL ======
    private static final String URL =
            "jdbc:mysql://localhost:3306/sparepart_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";
    // ===============================================

    private static DBConnection instance;   // the single instance
    private Connection connection;

    // private constructor: nobody else can do "new DBConnection()"
    private DBConnection() {
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // open a new connection only if we don't have a working one
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to the database. Check MySQL is running and the password in DBConnection.java.", e);
        }
    }
}