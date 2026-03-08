package com.ration.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ─────────────────────────────────────────────────────────────────
 *  DBConnection.java
 *  Utility class that provides a JDBC connection to PostgreSQL.
 *
 *  PostgreSQL Driver dependency (add to pom.xml):
 *  <dependency>
 *      <groupId>org.postgresql</groupId>
 *      <artifactId>postgresql</artifactId>
 *      <version>42.7.1</version>
 *  </dependency>
 * ─────────────────────────────────────────────────────────────────
 */
public class DBConnection {

    // ── PostgreSQL connection settings ────────────────────────────
    private static final String DB_URL      = "jdbc:postgresql://localhost:5432/ration_db";
    private static final String DB_USER     = "postgres";        // change to your DB username
    private static final String DB_PASSWORD = "your_db_password"; // change to your DB password

    // Load the PostgreSQL JDBC driver once when class is loaded
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "PostgreSQL JDBC Driver not found. Add postgresql JAR to classpath.\n" + e.getMessage()
            );
        }
    }

    // Prevent instantiation
    private DBConnection() {}

    /**
     * Returns a fresh JDBC Connection to PostgreSQL.
     * CALLER IS RESPONSIBLE for closing it (use try-with-resources).
     *
     * Usage:
     *   try (Connection conn = DBConnection.getConnection()) {
     *       // use conn
     *   }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}