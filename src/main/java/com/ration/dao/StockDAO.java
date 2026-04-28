package com.ration.dao;

import com.ration.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StockDAO {

    public List<Map<String, Object>> getAllStock() {
        String sql = "SELECT stock_id, item_name, quantity, last_updated, unit_price, threshold_limit "
                   + "FROM stock ORDER BY item_name";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(toMap(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StockDAO] getAllStock() error: " + e.getMessage());
        }

        return list;
    }

    public Map<String, Object> getStockById(int stockId) {
        String sql = "SELECT stock_id, item_name, quantity, last_updated, unit_price, threshold_limit "
                   + "FROM stock WHERE stock_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stockId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toMap(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StockDAO] getStockById() error: " + e.getMessage());
        }

        return null;
    }

    public List<Map<String, Object>> getLowStockItems() {
        String sql = "SELECT stock_id, item_name, quantity, last_updated, unit_price, threshold_limit "
                   + "FROM stock WHERE quantity <= threshold_limit ORDER BY quantity ASC";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(toMap(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StockDAO] getLowStockItems() error: " + e.getMessage());
        }

        return list;
    }

    private Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("stock_id", rs.getInt("stock_id"));
        row.put("item_name", rs.getString("item_name"));
        row.put("quantity", rs.getBigDecimal("quantity"));
        row.put("last_updated", rs.getTimestamp("last_updated"));
        row.put("unit_price", rs.getBigDecimal("unit_price"));
        row.put("threshold_limit", rs.getBigDecimal("threshold_limit"));
        return row;
    }
}
