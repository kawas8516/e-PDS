package com.ration.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC Driver not found: " + e.getMessage());
        }

        Properties fallbackProperties = loadDbProperties();

        DB_URL = resolveValue("DB_URL", "db.url", fallbackProperties);
        DB_USER = resolveValue("DB_USER", "db.user", fallbackProperties);
        DB_PASSWORD = resolveValue("DB_PASSWORD", "db.password", fallbackProperties);

        if (isBlank(DB_URL) || isBlank(DB_USER) || isBlank(DB_PASSWORD)) {
            throw new ExceptionInInitializerError(
                    "Database configuration is incomplete. Configure DB_URL, DB_USER, DB_PASSWORD as "
                            + "environment variables or in src/main/resources/db.properties."
            );
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static String resolveValue(String envKey, String propertyKey, Properties properties) {
        String envValue = System.getenv(envKey);
        if (!isBlank(envValue)) {
            return envValue.trim();
        }

        String propertyValue = properties.getProperty(propertyKey);
        if (!isBlank(propertyValue)) {
            return propertyValue.trim();
        }

        return null;
    }

    private static Properties loadDbProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load db.properties: " + e.getMessage());
        }

        return properties;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
