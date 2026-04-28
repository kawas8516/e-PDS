package com.ration.dao;

import com.ration.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CitizenDAO {

    public List<Map<String, Object>> getFamilyMembersByUser(int userId) {
        String sql = "SELECT fm.member_id, fm.name, fm.relation, fm.aadhaar, fm.dob, fm.gender, fm.age "
                   + "FROM family_members fm "
                   + "JOIN ration_cards rc ON fm.card_id = rc.card_id "
                   + "WHERE rc.user_id = ? "
                   + "ORDER BY fm.member_id DESC";

        List<Map<String, Object>> members = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("memberId", rs.getInt("member_id"));
                    row.put("name", rs.getString("name"));
                    row.put("relation", rs.getString("relation"));
                    row.put("aadhaar", rs.getString("aadhaar"));
                    row.put("dob", rs.getDate("dob"));
                    row.put("gender", rs.getString("gender"));
                    row.put("age", rs.getInt("age"));
                    members.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("[CitizenDAO] getFamilyMembersByUser() error: " + e.getMessage());
        }

        return members;
    }

    public int getOrCreateCardId(int userId) throws SQLException {
        String selectSql = "SELECT card_id FROM ration_cards WHERE user_id = ? ORDER BY card_id DESC LIMIT 1";
        String insertSql = "INSERT INTO ration_cards (card_number, user_id, card_type, status, issue_date) "
                         + "VALUES (?, ?, ?, 'ACTIVE', CURRENT_DATE) RETURNING card_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

            selectPs.setInt(1, userId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("card_id");
                }
            }

            String cardType = determineCardType(conn, userId);
            String cardNumber = "RC" + userId + System.currentTimeMillis();

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, cardNumber);
                insertPs.setInt(2, userId);
                insertPs.setString(3, cardType);

                try (ResultSet rs = insertPs.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("card_id");
                    }
                }
            }
        }

        throw new SQLException("Unable to find/create ration card for userId=" + userId);
    }

    public boolean insertFamilyMember(int cardId, String name, String relation, String aadhaar, String dob, String gender) {
        String sql = "INSERT INTO family_members (card_id, name, relation, aadhaar, dob, gender, age) "
                   + "VALUES (?, ?, ?, ?, ?, ?, EXTRACT(YEAR FROM AGE(CURRENT_DATE, ?::date))::int)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cardId);
            ps.setString(2, name);
            ps.setString(3, relation);
            ps.setString(4, aadhaar);
            ps.setDate(5, Date.valueOf(dob));
            ps.setString(6, gender);
            ps.setDate(7, Date.valueOf(dob));

            return ps.executeUpdate() > 0;

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("[CitizenDAO] insertFamilyMember() error: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getComplaintsByUser(int userId) {
        String sql = "SELECT complaint_id, complaint_type, month, description, status, created_at "
                   + "FROM complaints "
                   + "WHERE user_id = ? "
                   + "ORDER BY created_at DESC";

        List<Map<String, Object>> complaints = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("complaintId", rs.getInt("complaint_id"));
                    row.put("complaintType", rs.getString("complaint_type"));
                    row.put("month", rs.getString("month"));
                    row.put("description", rs.getString("description"));
                    row.put("status", rs.getString("status"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    complaints.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("[CitizenDAO] getComplaintsByUser() error: " + e.getMessage());
        }

        return complaints;
    }

    public boolean insertComplaint(int userId, String complaintType, String month, String description) {
        String sql = "INSERT INTO complaints (user_id, complaint_type, month, description, status) "
                   + "VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, complaintType);
            ps.setString(3, month);
            ps.setString(4, description);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[CitizenDAO] insertComplaint() error: " + e.getMessage());
            return false;
        }
    }

    private String determineCardType(Connection conn, int userId) {
        String sql = "SELECT annual_income FROM users WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long annualIncome = rs.getLong("annual_income");
                    return annualIncome <= 100000 ? "BPL" : "APL";
                }
            }
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] determineCardType() error: " + e.getMessage());
        }

        return "APL";
    }
}
