package com.ration.controller;

import com.ration.dao.UserDAO;
import com.ration.util.AuditUtil;
import com.ration.util.CSRFUtil;
import com.ration.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/ResetPasswordServlet")
public class ResetPasswordServlet extends HttpServlet {

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

        HttpSession session = request.getSession(true);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", CSRFUtil.generateToken());
        }

        request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String sessionToken = session == null ? null : (String) session.getAttribute("csrfToken");
        String requestToken = request.getParameter("csrfToken");

        if (!CSRFUtil.validateToken(sessionToken, requestToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        String identifier = trim(request.getParameter("identifier"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmPassword = trim(request.getParameter("confirmPassword"));

        if (isBlank(identifier)) {
            forwardWithError(request, response, "Username or email is required.", identifier);
            return;
        }

        if (isBlank(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            forwardWithError(request, response, "Password must be at least 6 characters.", identifier);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            forwardWithError(request, response, "Passwords do not match.", identifier);
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDAO.updatePassword(identifier, hashedPassword);

        if (!updated) {
            forwardWithError(request, response, "User not found.", identifier);
            return;
        }

        AuditUtil.logAction(0, "PASSWORD_RESET", request.getRemoteAddr());

        request.setAttribute("success", "Password reset successful. Please log in.");
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private void forwardWithError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String error,
                                  String identifier)
            throws ServletException, IOException {

        request.setAttribute("error", error);
        request.setAttribute("identifier", identifier == null ? "" : identifier);
        request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
