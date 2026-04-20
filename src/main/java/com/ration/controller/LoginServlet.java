package com.ration.controller;

import com.ration.model.User;
import com.ration.service.AuthService;
import com.ration.util.CSRFUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

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

        if (existingSession != null && existingSession.getAttribute("user") != null) {
            User user = (User) existingSession.getAttribute("user");
            response.sendRedirect(request.getContextPath() + authService.getDashboardPath(user));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isBlank(username)) {
            forwardWithError(request, response, "Username is required.", username);
            return;
        }

        if (isBlank(password)) {
            forwardWithError(request, response, "Password is required.", username);
            return;
        }

        User user = authService.authenticate(username, password);

        if (user == null) {
            forwardWithError(request, response, "Invalid username or password.", username);
            return;
        }

        // Invalidate old session
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Create new session
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute("csrfToken", CSRFUtil.generateToken());

        // Store user info
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole());

        // Redirect to dashboard
        response.sendRedirect(request.getContextPath() + authService.getDashboardPath(user));
    }

    private void forwardWithError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String errorMessage,
                                  String username)
            throws ServletException, IOException {

        request.setAttribute("error", errorMessage);
        request.setAttribute("lastUsername", username == null ? "" : username.trim());
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
