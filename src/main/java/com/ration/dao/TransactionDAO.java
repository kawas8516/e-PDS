package com.ration.dao;
import com.ration.util.DBConnection;
import java.sql.*;

/**
 * Handles the issuance of rations. 
 * This uses a SQL Transaction to ensure data integrity.
 */
public class TransactionDAO {

    public boolean issueRation(String cardId, int itemId, double qty, double amount) throws SQLException {
        String insertTxSql = "INSERT INTO transactions (card_id, item_id, quantity, amount, tx_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        String updateStockSql = "UPDATE stock_inventory SET quantity = quantity - ? WHERE item_id = ? AND quantity >= ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Deduct Stock
            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {
                stockStmt.setDouble(1, qty);
                stockStmt.setInt(2, itemId);
                stockStmt.setDouble(3, qty);
                int stockUpdated = stockStmt.executeUpdate();
                
                if (stockUpdated == 0) throw new SQLException("Insufficient stock!");
            }

            // 2. Record Transaction
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql)) {
                txStmt.setString(1, cardId);
                txStmt.setInt(2, itemId);
                txStmt.setDouble(3, qty);
                txStmt.setDouble(4, amount);
                txStmt.executeUpdate();
            }

            conn.commit(); // Finalize
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }
}