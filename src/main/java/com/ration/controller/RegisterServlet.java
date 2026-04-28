package com.ration.controller;

import com.ration.dao.UserDAO;
import com.ration.util.CSRFUtil;
import com.ration.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Seed CSRF token before rendering the registration form.
        HttpSession session = request.getSession(true);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", CSRFUtil.generateToken());
        }
        // Forward directly to the view — avoids the root register.jsp forward loop.
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CSRF validation.
        HttpSession session = request.getSession(false);
        String sessionToken = (session != null) ? (String) session.getAttribute("csrfToken") : null;
        String requestToken = request.getParameter("csrfToken");

        if (!CSRFUtil.validateToken(sessionToken, requestToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        String username = trim(request.getParameter("username"));
        String email = trim(request.getParameter("email"));
        String fullName = trim(request.getParameter("fullName"));
        String mobile = trim(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        long annualIncome = 0;
        try {
            String incomeStr = trim(request.getParameter("annualIncome"));
            if (incomeStr != null && !incomeStr.isEmpty()) {
                annualIncome = Long.parseLong(incomeStr.replaceAll("[^0-9]", ""));
            }
        } catch (NumberFormatException ignored) { }

        // --- Validation ---

        if (isBlank(username)) {
            forwardWithError(request, response, "Username is required.");
            return;
        }

        if (isBlank(email)) {
            forwardWithError(request, response, "Email is required.");
            return;
        }

        if (isBlank(fullName)) {
            forwardWithError(request, response, "Full name is required.");
            return;
        }

        if (isBlank(password) || password.length() < MIN_PASSWORD_LENGTH) {
            forwardWithError(request, response, "Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            forwardWithError(request, response, "Passwords do not match.");
            return;
        }

        // --- Check duplicate ---

        if (userDAO.findByUsername(username) != null) {
            forwardWithError(request, response, "Username already exists. Please choose another.");
            return;
        }

        // --- Register ---

        String hashedPassword = PasswordUtil.hashPassword(password);
        try {
            boolean success = userDAO.registerUser(username, hashedPassword, fullName, email, mobile, annualIncome);
            if (success) {
                request.setAttribute("successMessage", "Registration successful! You can now log in.");
                request.getRequestDispatcher("/WEB-INF/views/register-success.jsp").forward(request, response);
            } else {
                forwardWithError(request, response, "Registration failed. Please try again.");
            }
        } catch (RuntimeException e) {
            System.err.println("[RegisterServlet] Registration threw: " + e.getMessage());
            forwardWithError(request, response,
                "Registration failed due to a database error. Check the server console for details.");
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        // Regenerate CSRF token for the retry form.
        HttpSession session = request.getSession(true);
        session.setAttribute("csrfToken", CSRFUtil.generateToken());

        request.setAttribute("error", error);
        request.setAttribute("prevUsername", trim(request.getParameter("username")));
        request.setAttribute("prevEmail", trim(request.getParameter("email")));
        request.setAttribute("prevFullName", trim(request.getParameter("fullName")));
        request.setAttribute("prevPhone", trim(request.getParameter("phone")));
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
