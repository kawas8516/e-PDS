package com.ration.controller;

import java.io.IOException;
import com.ration.dao.UserDAO;
import com.ration.model.User;
import com.ration.util.AuditUtil;
import com.ration.util.CSRFUtil;
import com.ration.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


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

        // Forward directly to view — avoids the root reset-password.jsp forward loop.
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
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
        // Security: require the current (known) password before accepting a new one.
        // This prevents the unauthenticated account-takeover path: an attacker who
        // knows only a username or email cannot set a new password without the existing one.
        // A full email-token reset flow is the proper long-term fix (out of scope here).
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmPassword = trim(request.getParameter("confirmPassword"));

        if (isBlank(identifier)) {
            forwardWithError(request, response, "Username or email is required.", identifier);
            return;
        }

        if (isBlank(currentPassword)) {
            forwardWithError(request, response, "Current password is required.", identifier);
            return;
        }

        if (isBlank(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            forwardWithError(request, response, "New password must be at least 6 characters.", identifier);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            forwardWithError(request, response, "Passwords do not match.", identifier);
            return;
        }

        // Look up the user to verify identity and get the real userId for audit.
        User user = userDAO.findByIdentifier(identifier);
        if (user == null) {
            forwardWithError(request, response, "User not found.", identifier);
            return;
        }

        // Verify the current password against the stored hash before allowing reset.
        User verified = userDAO.authenticate(user.getUsername(), currentPassword);
        if (verified == null) {
            forwardWithError(request, response, "Current password is incorrect.", identifier);
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDAO.updatePassword(identifier, hashedPassword);

        if (!updated) {
            forwardWithError(request, response, "Password update failed. Please try again.", identifier);
            return;
        }

        // Audit with the real userId now that the user is resolved.
        AuditUtil.logAction(user.getUserId(), "PASSWORD_RESET", request.getRemoteAddr());

        request.setAttribute("success", "Password reset successful. Please log in.");
        // Forward directly to view — root index.jsp triggers an AuthFilter redirect loop.
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }

    private void forwardWithError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String error,
                                  String identifier)
            throws ServletException, IOException {

        // Regenerate CSRF token for the retry form.
        HttpSession session = request.getSession(true);
        session.setAttribute("csrfToken", CSRFUtil.generateToken());

        request.setAttribute("error", error);
        request.setAttribute("identifier", identifier == null ? "" : identifier);
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
