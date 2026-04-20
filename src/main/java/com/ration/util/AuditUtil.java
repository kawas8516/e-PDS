package com.ration.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AuditUtil {

    private AuditUtil() {
    }

    public static void logAction(int userId, String action, String ip) {
        String sql = "INSERT INTO audit_logs (user_id, action, timestamp, ip_address) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, action);
            statement.setString(3, ip == null ? "" : ip);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[AuditUtil] logAction() SQL error: " + e.getMessage());
        }
    }
}
