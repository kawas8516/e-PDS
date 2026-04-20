package com.ration.controller;

import com.ration.model.User;
import com.ration.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        User user = existingSession == null ? null : (User) existingSession.getAttribute("user");

        if (user != null) {
            redirectByRole(user, request, response);
            return;
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isBlank(username)) {
            forwardLoginError(request, response, "Username or Aadhaar number is required.", username);
            return;
        }

        if (isBlank(password)) {
            forwardLoginError(request, response, "Password is required.", username);
            return;
        }

        User user = authService.authenticate(username, password);
        if (user == null) {
            forwardLoginError(request, response, "Invalid username or password. Please try again.", username);
            return;
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute("user", user);

        redirectByRole(user, request, response);
    }

    private void redirectByRole(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/citizen-dashboard.jsp");
        }
    }

    private void forwardLoginError(HttpServletRequest request,
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
