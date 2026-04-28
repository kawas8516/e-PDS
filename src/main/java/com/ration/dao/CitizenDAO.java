package com.ration.dao;

import com.ration.util.DBConnection;

<<<<<<< HEAD
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

=======
import java.sql.*;
import java.util.*;

public class CitizenDAO {

    // ── Complaints ────────────────────────────────────────────────

    public boolean insertComplaint(int userId, String type, String month, String description) {
        String sql = "INSERT INTO complaints (user_id, complaint_type, month, description, status) "
                   + "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setString(3, month);
            stmt.setString(4, description);
            return stmt.executeUpdate() > 0;
>>>>>>> af66db6 (issues fix)
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] insertComplaint() error: " + e.getMessage());
            return false;
        }
    }

<<<<<<< HEAD
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
=======
    public List<Map<String, Object>> getComplaintsByUser(int userId) {
        String sql = "SELECT complaint_id, complaint_type, month, description, status, created_at "
                   + "FROM complaints WHERE user_id = ? ORDER BY created_at DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("complaint_id"));
                    row.put("type", rs.getString("complaint_type"));
                    row.put("month", rs.getString("month"));
                    row.put("description", rs.getString("description"));
                    row.put("status", rs.getString("status"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] getComplaintsByUser() error: " + e.getMessage());
        }
        return list;
    }

    // ── Ration Card ───────────────────────────────────────────────

    private static final long BPL_INCOME_THRESHOLD = 100_000L;

    public int getOrCreateCardId(int userId) throws SQLException {
        String selectSql = "SELECT card_id FROM ration_cards WHERE user_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("card_id");
            }
        }
        // Determine card type from the citizen's registered annual income
        long income = 0;
        String incomeSql = "SELECT annual_income FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(incomeSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) income = rs.getLong("annual_income");
            }
        }
        String cardType = (income <= BPL_INCOME_THRESHOLD) ? "BPL" : "APL";
        String cardNumber = "RC-" + java.time.Year.now().getValue() + "-" + userId
                          + "-" + String.format("%04d", (int)(Math.random() * 10000));
        String insertSql = "INSERT INTO ration_cards (card_number, user_id, card_type, status) "
                         + "VALUES (?, ?, ?, 'ACTIVE') RETURNING card_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, cardNumber);
            stmt.setInt(2, userId);
            stmt.setString(3, cardType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("card_id");
            }
        }
        throw new SQLException("Failed to create ration card for user " + userId);
    }

    // ── Family Members ────────────────────────────────────────────

    public boolean insertFamilyMember(int cardId, String name, String relation,
                                      String aadhaar, String dob, String gender) {
        // Calculate age from dob (approximate)
        int age = 0;
        if (dob != null && !dob.isEmpty()) {
            try {
                java.time.LocalDate birth = java.time.LocalDate.parse(dob);
                age = java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
            } catch (Exception ignored) { }
        }
        String sql = "INSERT INTO family_members (card_id, name, relation, aadhaar, dob, gender, age) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            stmt.setString(2, name);
            stmt.setString(3, relation);
            stmt.setString(4, aadhaar);
            if (dob != null && !dob.isEmpty()) {
                stmt.setDate(5, java.sql.Date.valueOf(dob));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            stmt.setString(6, gender);
            stmt.setInt(7, age);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] insertFamilyMember() error: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getFamilyMembersByUser(int userId) {
        String sql = "SELECT fm.member_id, fm.name, fm.relation, fm.aadhaar, fm.dob, fm.gender, fm.age "
                   + "FROM family_members fm "
                   + "JOIN ration_cards rc ON fm.card_id = rc.card_id "
                   + "WHERE rc.user_id = ? ORDER BY fm.member_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("member_id"));
                    row.put("name", rs.getString("name"));
                    row.put("relation", rs.getString("relation"));
                    row.put("aadhaar", rs.getString("aadhaar"));
                    row.put("dob", rs.getDate("dob"));
                    row.put("gender", rs.getString("gender"));
                    row.put("age", rs.getInt("age"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] getFamilyMembersByUser() error: " + e.getMessage());
        }
        return list;
    }

    // ── Admin: all families ───────────────────────────────────────

    public List<Map<String, Object>> getAllFamilies() {
        String sql = "SELECT u.user_id, u.full_name, u.username, u.email, u.mobile, "
                   + "       COALESCE(u.annual_income, 0) AS annual_income, "
                   + "       rc.card_id, rc.card_number, rc.card_type, rc.status, "
                   + "       COUNT(fm.member_id) AS member_count "
                   + "FROM users u "
                   + "LEFT JOIN ration_cards rc ON u.user_id = rc.user_id "
                   + "LEFT JOIN family_members fm ON rc.card_id = fm.card_id "
                   + "WHERE u.role = 'CITIZEN' "
                   + "GROUP BY u.user_id, u.full_name, u.username, u.email, u.mobile, u.annual_income, "
                   + "         rc.card_id, rc.card_number, rc.card_type, rc.status "
                   + "ORDER BY u.full_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId",       rs.getInt("user_id"));
                row.put("fullName",     rs.getString("full_name"));
                row.put("username",     rs.getString("username"));
                row.put("email",        rs.getString("email"));
                row.put("mobile",       rs.getString("mobile"));
                row.put("annualIncome", rs.getLong("annual_income"));
                row.put("cardId",       rs.getObject("card_id"));
                row.put("cardNumber",   rs.getString("card_number"));
                row.put("cardType",     rs.getString("card_type"));
                row.put("cardStatus",   rs.getString("status"));
                row.put("memberCount",  rs.getInt("member_count"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] getAllFamilies() error: " + e.getMessage());
        }
        return list;
    }

    public List<Map<String, Object>> getFamilyMembersByCard(int cardId) {
        String sql = "SELECT member_id, name, relation, aadhaar, dob, gender, age "
                   + "FROM family_members WHERE card_id = ? ORDER BY member_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",       rs.getInt("member_id"));
                    row.put("name",     rs.getString("name"));
                    row.put("relation", rs.getString("relation"));
                    row.put("aadhaar",  rs.getString("aadhaar"));
                    row.put("dob",      rs.getDate("dob"));
                    row.put("gender",   rs.getString("gender"));
                    row.put("age",      rs.getInt("age"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] getFamilyMembersByCard() error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateCardType(int cardId, String cardType) {
        String sql = "UPDATE ration_cards SET card_type = ? WHERE card_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cardType);
            stmt.setInt(2, cardId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] updateCardType() error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateFamilyIncome(int userId, long annualIncome) {
        String sql = "UPDATE users SET annual_income = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, annualIncome);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] updateFamilyIncome() error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFamilyMember(int memberId) {
        String sql = "DELETE FROM family_members WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CitizenDAO] deleteFamilyMember() error: " + e.getMessage());
            return false;
        }
>>>>>>> af66db6 (issues fix)
    }
}
