package com.ration.controller;

import com.ration.dao.UserDAO;
import com.ration.model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ═══════════════════════════════════════════════════════════════════
 *  LoginServlet.java
 *  Handles POST from index.html login form.
 *
 *  Flow:
 *  1. Read username + password from request
 *  2. Validate inputs (not empty, length)
 *  3. Call UserDAO.authenticate() → hits PostgreSQL via JDBC
 *     - UserDAO fetches password_hash from DB by username
 *     - BCrypt.checkpw(plainPassword, storedHash) inside PasswordUtil
 *  4. On success → create HttpSession, store User object, redirect
 *  5. On failure → forward back to index.html with error message
 *
 *  Dependencies (pom.xml):
 *    - org.postgresql:postgresql:42.7.1
 *    - org.mindrot:jbcrypt:0.4
 * ═══════════════════════════════════════════════════════════════════
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Session timeout: 30 minutes of inactivity
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private UserDAO userDAO;

    /**
     * Called once when Servlet is first loaded by the container.
     * Safe place to initialize DAO (not in constructor).
     */
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    // ─────────────────────────────────────────────────────────────
    //  GET /LoginServlet
    //  If someone directly visits /LoginServlet URL, redirect to
    //  login page. Also handles already-logged-in users.
    // ─────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already logged in, skip login page
        HttpSession existingSession = request.getSession(false); // false = don't create
        if (existingSession != null && existingSession.getAttribute("loggedInUser") != null) {
            User user = (User) existingSession.getAttribute("loggedInUser");
            if (user.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/citizen/dashboard.jsp");
            }
            return;
        }

        // Not logged in → show login page
        response.sendRedirect(request.getContextPath() + "/index.html");
    }

    // ─────────────────────────────────────────────────────────────
    //  POST /LoginServlet
    //  Called when index.html form submits.
    //  Reads: username, password, role (from hidden input)
    // ─────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── Step 1: Read form parameters ─────────────────────────
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role     = request.getParameter("role"); // "citizen" or "admin" from hidden input

        // ── Step 2: Input validation (before hitting DB) ─────────
        if (username == null || username.trim().isEmpty()) {
            forwardWithError(request, response, "Username or Aadhaar number is required.");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            forwardWithError(request, response, "Password is required.");
            return;
        }
        if (password.length() < 6 || password.length() > 100) {
            forwardWithError(request, response, "Invalid credentials.");
            return;
        }

        username = username.trim();

        // ── Step 3: JDBC Authentication via UserDAO ───────────────
        //
        //  What happens inside userDAO.authenticate():
        //    SQL: SELECT ... FROM users WHERE LOWER(username) = LOWER(?) AND is_active = TRUE
        //    Then: BCrypt.checkpw(password, storedHash) → true/false
        //
        User user = userDAO.authenticate(username, password);

        // ── Step 4a: Authentication FAILED ───────────────────────
        if (user == null) {
            // Generic message — never say "wrong password" or "user not found"
            // (that tells attackers which part was wrong)
            forwardWithError(request, response, "Invalid username or password. Please try again.");
            return;
        }

        // ── Step 4b: Role mismatch check ─────────────────────────
        // User exists and password matched, but wrong portal button clicked?
        if (role != null && !role.isEmpty() && !user.getRole().equalsIgnoreCase(role)) {
            forwardWithError(request, response,
                "This account is not registered as " + role + ". Please use the correct portal.");
            return;
        }

        // ── Step 5: Create secure HttpSession ────────────────────
        //
        //  Invalidate any old session first (session fixation protection)
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Create a brand new session
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        // Store User object in session (DO NOT store password hash here)
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId",       user.getUserId());
        session.setAttribute("username",     user.getUsername());
        session.setAttribute("fullName",     user.getFullName());
        session.setAttribute("role",         user.getRole());

        // ── Step 6: Redirect based on role ───────────────────────
        if (user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/citizen/dashboard.jsp");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helper: forward back to index.html with an error message.
    //
    //  Sets request attribute "error" so JSP can display it:
    //    <%= request.getAttribute("error") %>
    //    or with JSTL: ${error}
    // ─────────────────────────────────────────────────────────────
    private void forwardWithError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String errorMessage)
            throws ServletException, IOException {

        request.setAttribute("error", errorMessage);

        // Preserve what user typed so they don't have to re-enter it
        request.setAttribute("lastUsername", request.getParameter("username"));

        // Forward (not redirect) so the error attribute survives the trip
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}