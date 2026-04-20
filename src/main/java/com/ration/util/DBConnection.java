package com.ration.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Class.forName("org.postgresql.Driver");
            loadDbProperties();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC Driver not found: " + e.getMessage());
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Unable to load db.properties: " + e.getMessage());
        }

        if (isBlank(url) || isBlank(user) || isBlank(password)) {
            throw new ExceptionInInitializerError("Missing db.url, db.user, or db.password in db.properties.");
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadDbProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IOException("db.properties not found in classpath.");
            }
            properties.load(inputStream);
        }

        url = trimToNull(properties.getProperty("db.url"));
        user = trimToNull(properties.getProperty("db.user"));
        password = trimToNull(properties.getProperty("db.password"));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
