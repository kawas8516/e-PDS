package com.ration.controller;

import com.ration.dao.UserDAO;
import com.ration.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        // Show the registration form
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = trim(request.getParameter("username"));
        String email = trim(request.getParameter("email"));
        String fullName = trim(request.getParameter("fullName"));
        String mobile = trim(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

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
            boolean success = userDAO.registerUser(username, hashedPassword, fullName, email, mobile);
            if (success) {
                request.setAttribute("successMessage", "Registration successful! You can now log in.");
                request.getRequestDispatcher("/WEB-INF/views/register-success.jsp").forward(request, response);
            } else {
                forwardWithError(request, response, "Registration failed. Please try again.");
            }
        } catch (RuntimeException e) {
            // DB error — already fully logged in UserDAO; show a safe message to the user.
            System.err.println("[RegisterServlet] Registration threw: " + e.getMessage());
            forwardWithError(request, response,
                "Registration failed due to a database error. Check the server console for details.");
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        // Preserve form values so user doesn't retype everything
        request.setAttribute("prevUsername", trim(request.getParameter("username")));
        request.setAttribute("prevEmail", trim(request.getParameter("email")));
        request.setAttribute("prevFullName", trim(request.getParameter("fullName")));
        request.setAttribute("prevPhone", trim(request.getParameter("phone")));
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
