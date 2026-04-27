package com.ration.dao;

import com.ration.model.User;
import com.ration.util.DBConnection;
import com.ration.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ─────────────────────────────────────────────────────────────────
 *  UserDAO.java  (Data Access Object)
 *
 *  Handles all DB operations for the "users" table:
 *    - authenticate()      → used by LoginServlet
 *    - findByUsername()    → helper
 *    - updateLastLogin()   → called after successful login
 *
 *  Uses PreparedStatement everywhere → prevents SQL Injection.
 *  Uses BCrypt via PasswordUtil     → secure password check.
 * ─────────────────────────────────────────────────────────────────
 */
public class UserDAO {

    // ─────────────────────────────────────────────────────────────
    //  authenticate()
    //  Called by LoginServlet.doPost()
    //
    //  Flow:
    //  1. Fetch user row from DB by username (case-insensitive)
    //  2. Check account is active
    //  3. Use BCrypt to verify plain password against stored hash
    //  4. Return User object on success, null on failure
    // ─────────────────────────────────────────────────────────────
    public User authenticate(String username, String plainPassword) {

        // SQL: fetch user by username only (NOT by password — hash check is in Java)
        String sql = "SELECT user_id, username, password_hash, full_name, " +
                     "       email, mobile, role, is_active " +
                     "FROM   users " +
                     "WHERE  LOWER(username) = LOWER(?) " +   // case-insensitive match
                     "AND    is_active = TRUE";

        // try-with-resources → auto-closes conn, stmt, rs
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind parameter — PreparedStatement prevents SQL injection
            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    // Pull stored BCrypt hash from DB
                    String storedHash = rs.getString("password_hash");

                    // BCrypt comparison: plain password vs hash in DB
                    // This is the ONLY place plain password is used
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {

                        // Build and return User object (without exposing hash)
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setPasswordHash(storedHash); // kept internally, not sent to JSP
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setMobile(rs.getString("mobile"));
                        user.setRole(rs.getString("role"));
                        user.setActive(rs.getBoolean("is_active"));

                        // Update last_login timestamp in background
                        updateLastLogin(user.getUserId());

                        return user; // ✅ Login success

                    } else {
                        return null; // ❌ Password mismatch
                    }

                } else {
                    return null; // ❌ Username not found or account inactive
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate() SQL error: " + e.getMessage());
            e.printStackTrace();
            return null; // Treat DB error as failed login (don't expose internals)
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  findByUsername()
    //  Useful for checking if a username already exists (registration)
    // ─────────────────────────────────────────────────────────────
    public User findByUsername(String username) {

        String sql = "SELECT user_id, username, full_name, email, mobile, role, is_active " +
                     "FROM   users " +
                     "WHERE  LOWER(username) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setMobile(rs.getString("mobile"));
                    user.setRole(rs.getString("role"));
                    user.setActive(rs.getBoolean("is_active"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] findByUsername() SQL error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    // ─────────────────────────────────────────────────────────────
    //  findByIdentifier()
    //  Looks up a user by username OR email (used by ResetPasswordServlet
    //  to resolve the real userId before writing the audit log entry).
    // ─────────────────────────────────────────────────────────────
    public User findByIdentifier(String usernameOrEmail) {
        String sql = "SELECT user_id, username, full_name, email, mobile, role, is_active " +
                     "FROM   users " +
                     "WHERE  LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameOrEmail.trim());
            stmt.setString(2, usernameOrEmail.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setMobile(rs.getString("mobile"));
                    user.setRole(rs.getString("role"));
                    user.setActive(rs.getBoolean("is_active"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] findByIdentifier() SQL error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean updatePassword(String usernameOrEmail, String newHashedPassword) {

        String sql = "UPDATE users SET password_hash = ? " +
                     "WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHashedPassword);
            stmt.setString(2, usernameOrEmail.trim());
            stmt.setString(3, usernameOrEmail.trim());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword() SQL error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  updateLastLogin()
    //  Records the timestamp of the last successful login
    // ─────────────────────────────────────────────────────────────
    private void updateLastLogin(int userId) {

        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            // Non-critical — don't fail login just because this update failed
            System.err.println("[UserDAO] updateLastLogin() SQL error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  registerUser()
    //  Inserts a new citizen user into the database.
    //  Returns true if insert succeeded, false otherwise.
    // ─────────────────────────────────────────────────────────────
    public boolean registerUser(String username, String hashedPassword,
                                String fullName, String email, String mobile) {

        // Role stored as uppercase to match RBAC checks in AuthFilter and AuthService.
        String sql = "INSERT INTO users (username, password_hash, full_name, email, mobile, role, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, 'CITIZEN', TRUE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, mobile);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] registerUser() FAILED — SQLState: " + e.getSQLState()
                + " | Code: " + e.getErrorCode()
                + " | Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("DB insert failed: " + e.getMessage(), e);
        }
    }
}