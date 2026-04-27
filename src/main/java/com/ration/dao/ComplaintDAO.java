package com.ration.dao;

import com.ration.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ComplaintDAO {

    public boolean createComplaint(int userId, String type, String month, String description) {
        String[] sqlCandidates = {
                "INSERT INTO complaints (user_id, complaint_type, related_month, description, status, created_at) VALUES (?, ?, ?, ?, 'OPEN', CURRENT_TIMESTAMP)",
                "INSERT INTO complaints (user_id, type, month, description, status, created_at) VALUES (?, ?, ?, ?, 'OPEN', CURRENT_TIMESTAMP)",
                "INSERT INTO complaints (user_id, complaint_type, related_month, description, status) VALUES (?, ?, ?, ?, 'OPEN')",
                "INSERT INTO complaints (user_id, type, month, description, status) VALUES (?, ?, ?, ?, 'OPEN')"
        };

        for (String sql : sqlCandidates) {
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, userId);
                statement.setString(2, type);
                statement.setString(3, month);
                statement.setString(4, description);

                return statement.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("[ComplaintDAO] insert attempt failed: " + e.getMessage());
            }
        }

        return false;
    }
}
