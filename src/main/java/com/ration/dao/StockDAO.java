package com.ration.dao;

import com.ration.util.DBConnection;
import java.sql.*;
import java.util.*;

public class StockDAO {

    public List<Map<String, Object>> getAllStock() {
        String sql = "SELECT stock_id, item_name, quantity, unit_price, threshold_limit, last_updated "
                   + "FROM stock ORDER BY item_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("itemId",         rs.getInt("stock_id"));
                row.put("itemName",       rs.getString("item_name"));
                row.put("quantity",       rs.getInt("quantity"));
                row.put("unitPrice",      rs.getBigDecimal("unit_price"));
                row.put("thresholdLimit", rs.getInt("threshold_limit"));
                row.put("lastUpdated",    rs.getTimestamp("last_updated"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[StockDAO] getAllStock() error: " + e.getMessage());
        }
        return list;
    }

    public boolean addStock(String itemName, int quantity, double unitPrice, int thresholdLimit) {
        String sql = "INSERT INTO stock (item_name, quantity, unit_price, threshold_limit, last_updated) "
                   + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemName);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, unitPrice);
            stmt.setInt(4, thresholdLimit);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StockDAO] addStock() error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateQuantity(int stockId, int newQuantity) {
        String sql = "UPDATE stock SET quantity = ?, last_updated = CURRENT_TIMESTAMP WHERE stock_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, stockId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StockDAO] updateQuantity() error: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePrice(int stockId, double unitPrice) {
        String sql = "UPDATE stock SET unit_price = ?, last_updated = CURRENT_TIMESTAMP WHERE stock_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, unitPrice);
            stmt.setInt(2, stockId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StockDAO] updatePrice() error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateThreshold(int stockId, int thresholdLimit) {
        String sql = "UPDATE stock SET threshold_limit = ?, last_updated = CURRENT_TIMESTAMP WHERE stock_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, thresholdLimit);
            stmt.setInt(2, stockId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StockDAO] updateThreshold() error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStock(int stockId) {
        String sql = "DELETE FROM stock WHERE stock_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stockId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StockDAO] deleteStock() error: " + e.getMessage());
            return false;
        }
    }
}
