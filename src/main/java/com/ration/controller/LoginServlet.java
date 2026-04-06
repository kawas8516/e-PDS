package com.ration.controller;

import com.ration.model.User;
import com.ration.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null && existingSession.getAttribute("loggedInUser") != null) {
            User user = (User) existingSession.getAttribute("loggedInUser");
            response.sendRedirect(request.getContextPath() + authService.getDashboardPath(user));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/index.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isBlank(username)) {
            redirectWithError(request, response, "Username or Aadhaar number is required.", username);
            return;
        }

        if (isBlank(password)) {
            redirectWithError(request, response, "Password is required.", username);
            return;
        }

        User user = authService.authenticate(username, password);

        if (user == null) {
            redirectWithError(request, response, "Invalid username or password. Please try again.", username);
            return;
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole());

        response.sendRedirect(request.getContextPath() + authService.getDashboardPath(user));
    }

    private void redirectWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String errorMessage,
                                   String username)
            throws IOException {

        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.name());
        String encodedUsername = URLEncoder.encode(
                username == null ? "" : username.trim(),
                StandardCharsets.UTF_8.name()
        );

        response.sendRedirect(request.getContextPath() + "/index.html?error=" + encodedError
                + "&lastUsername=" + encodedUsername);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
