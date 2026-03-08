package com.ration.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * ─────────────────────────────────────────────────────────────────
 *  PasswordUtil.java
 *  Handles BCrypt password hashing and verification.
 *
 *  BCrypt dependency (add to pom.xml):
 *  <dependency>
 *      <groupId>org.mindrot</groupId>
 *      <artifactId>jbcrypt</artifactId>
 *      <version>0.4</version>
 *  </dependency>
 *
 *  How BCrypt works:
 *  - hashPassword("abc123")  → "$2a$12$xyz..." (a new hash every time due to salt)
 *  - verifyPassword("abc123", "$2a$12$xyz...") → true
 *  - verifyPassword("wrong",  "$2a$12$xyz...") → false
 *
 *  The hash is stored in the DB. Plain password is NEVER stored.
 * ─────────────────────────────────────────────────────────────────
 */
public class PasswordUtil {

    // Work factor: 12 = ~300ms per hash (safe against brute force)
    private static final int BCRYPT_ROUNDS = 12;

    // Prevent instantiation
    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using BCrypt.
     * Call this when REGISTERING a new user.
     *
     * @param plainPassword  the raw password entered by user
     * @return               BCrypt hash to store in the database
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     * Call this during LOGIN.
     *
     * @param plainPassword   the raw password entered by user at login
     * @param hashedPassword  the BCrypt hash stored in the database
     * @return                true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Invalid hash format — treat as mismatch
            return false;
        }
    }
}