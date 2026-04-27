package com.ration.controller;

import com.ration.model.User;
import com.ration.service.AuthService;
import com.ration.util.AuditUtil;
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

        // Already authenticated — bounce to the appropriate dashboard.
        if (existingSession != null && existingSession.getAttribute("user") != null) {
            User user = (User) existingSession.getAttribute("user");
            response.sendRedirect(request.getContextPath() + authService.getDashboardPath(user));
            return;
        }

        // Seed the CSRF token before the login form is rendered so the hidden
        // input has a value on the very first GET (fix for empty-token bug).
        HttpSession session = request.getSession(true);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", CSRFUtil.generateToken());
        }

        // Forward directly to the view — avoids the root index.jsp → AuthFilter loop.
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CSRF validation — must occur before any processing.
        HttpSession session = request.getSession(false);
        String sessionToken = (session != null) ? (String) session.getAttribute("csrfToken") : null;
        String requestToken = request.getParameter("csrfToken");

        if (!CSRFUtil.validateToken(sessionToken, requestToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

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
            // Audit failed login attempt — userId 0 is acceptable here since user is not resolved.
            AuditUtil.logAction(0, "LOGIN_FAIL", request.getRemoteAddr());
            forwardWithError(request, response, "Invalid username or password.", username);
            return;
        }

        // Session-fixation prevention: changeSessionId() preserves attributes while
        // issuing a new session ID, so the attacker's pre-auth session ID is invalidated.
        request.changeSessionId();

        HttpSession newSession = request.getSession(true);
        newSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        newSession.setAttribute("csrfToken", CSRFUtil.generateToken());

        // Store user info in session.
        newSession.setAttribute("user", user);
        newSession.setAttribute("userId", user.getUserId());
        newSession.setAttribute("username", user.getUsername());
        newSession.setAttribute("fullName", user.getFullName());
        newSession.setAttribute("role", user.getRole());

        // Audit successful login.
        AuditUtil.logAction(user.getUserId(), "LOGIN_SUCCESS", request.getRemoteAddr());

        response.sendRedirect(request.getContextPath() + authService.getDashboardPath(user));
    }

    private void forwardWithError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String errorMessage,
                                  String username)
            throws ServletException, IOException {

        // Regenerate CSRF token so retry form has a valid token.
        HttpSession session = request.getSession(true);
        session.setAttribute("csrfToken", CSRFUtil.generateToken());

        request.setAttribute("error", errorMessage);
        request.setAttribute("lastUsername", username == null ? "" : username.trim());
        // Forward directly to view — root index.jsp re-forwards here and triggers AuthFilter.
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
