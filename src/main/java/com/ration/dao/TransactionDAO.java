package com.ration.dao;

import com.ration.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    /**
     * Atomically deducts stock and records the transaction.
     * Uses a single DB connection with autoCommit=false so both operations
     * succeed or both roll back — no partial state.
     *
     * @throws SQLException with a user-readable message on insufficient stock or DB error
     */
    public boolean issueRation(int cardId, int itemId, int quantity, double amount, int issuedBy)
            throws SQLException {

        String updateStock = "UPDATE stock "
                           + "SET quantity = quantity - ?, last_updated = CURRENT_TIMESTAMP "
                           + "WHERE stock_id = ? AND quantity >= ?";
        String insertTx = "INSERT INTO transactions (card_id, item_id, quantity, amount, issued_by) "
                        + "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Step 1 — deduct stock (guarded by quantity >= ? to prevent negative stock)
            try (PreparedStatement ps = conn.prepareStatement(updateStock)) {
                ps.setInt(1, quantity);
                ps.setInt(2, itemId);
                ps.setInt(3, quantity);
                if (ps.executeUpdate() == 0)
                    throw new SQLException("Insufficient stock for stock_id=" + itemId);
            }

            // Step 2 — record transaction
            try (PreparedStatement ps = conn.prepareStatement(insertTx)) {
                ps.setInt(1, cardId);
                ps.setInt(2, itemId);
                ps.setInt(3, quantity);
                ps.setBigDecimal(4, BigDecimal.valueOf(amount));
                ps.setInt(5, issuedBy);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /** Transaction history for a citizen's ration card — newest first. */
    public List<Map<String, Object>> getTransactionsByCard(int cardId) {
        String sql = "SELECT t.tx_id, t.tx_date, t.quantity, t.amount, t.notes, "
                   + "       s.item_name, u.full_name AS issued_by_name "
                   + "FROM transactions t "
                   + "JOIN stock s ON t.item_id = s.stock_id "
                   + "LEFT JOIN users u ON t.issued_by = u.user_id "
                   + "WHERE t.card_id = ? "
                   + "ORDER BY t.created_at DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("txId",         rs.getInt("tx_id"));
                    row.put("txDate",       rs.getDate("tx_date"));
                    row.put("itemName",     rs.getString("item_name"));
                    row.put("quantity",     rs.getInt("quantity"));
                    row.put("amount",       rs.getBigDecimal("amount"));
                    row.put("issuedBy",     rs.getString("issued_by_name"));
                    row.put("notes",        rs.getString("notes"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getTransactionsByCard() error: " + e.getMessage());
        }
        return list;
    }

    /** All transactions — for admin view, newest first. */
    public List<Map<String, Object>> getAllTransactions() {
        String sql = "SELECT t.tx_id, t.tx_date, t.quantity, t.amount, "
                   + "       rc.card_number, holder.full_name AS holder_name, rc.card_type, "
                   + "       s.item_name, admin.full_name AS issued_by_name "
                   + "FROM transactions t "
                   + "JOIN ration_cards rc  ON t.card_id  = rc.card_id "
                   + "JOIN users holder     ON rc.user_id = holder.user_id "
                   + "JOIN stock s          ON t.item_id  = s.stock_id "
                   + "LEFT JOIN users admin ON t.issued_by = admin.user_id "
                   + "ORDER BY t.created_at DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("txId",         rs.getInt("tx_id"));
                row.put("txDate",       rs.getDate("tx_date"));
                row.put("cardNumber",   rs.getString("card_number"));
                row.put("holderName",   rs.getString("holder_name"));
                row.put("cardType",     rs.getString("card_type"));
                row.put("itemName",     rs.getString("item_name"));
                row.put("quantity",     rs.getInt("quantity"));
                row.put("amount",       rs.getBigDecimal("amount"));
                row.put("issuedBy",     rs.getString("issued_by_name"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getAllTransactions() error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Look up a ration card by its card_number.
     * Returns null when no active card with that number exists.
     */
    public Map<String, Object> getCardByNumber(String cardNumber) {
        String sql = "SELECT rc.card_id, rc.card_number, rc.card_type, rc.status, "
                   + "       u.full_name, u.mobile "
                   + "FROM ration_cards rc "
                   + "JOIN users u ON rc.user_id = u.user_id "
                   + "WHERE rc.card_number = ? AND rc.status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("cardId",     rs.getInt("card_id"));
                    row.put("cardNumber", rs.getString("card_number"));
                    row.put("cardType",   rs.getString("card_type"));
                    row.put("status",     rs.getString("status"));
                    row.put("holderName", rs.getString("full_name"));
                    row.put("mobile",     rs.getString("mobile"));
                    return row;
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getCardByNumber() error: " + e.getMessage());
        }
        return null;
    }

    /** All active ration cards — for admin issuance dropdown. */
    public List<Map<String, Object>> getAllActiveCards() {
        String sql = "SELECT rc.card_id, rc.card_number, rc.card_type, u.full_name "
                   + "FROM ration_cards rc "
                   + "JOIN users u ON rc.user_id = u.user_id "
                   + "WHERE rc.status = 'ACTIVE' "
                   + "ORDER BY u.full_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("cardId",     rs.getInt("card_id"));
                row.put("cardNumber", rs.getString("card_number"));
                row.put("cardType",   rs.getString("card_type"));
                row.put("holderName", rs.getString("full_name"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getAllActiveCards() error: " + e.getMessage());
        }
        return list;
    }
}
