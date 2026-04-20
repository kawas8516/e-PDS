package com.ration.controller;

import com.ration.model.User;
import com.ration.util.AuditUtil;
import com.ration.util.CSRFUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                AuditUtil.logAction(user.getUserId(), "LOGOUT", request.getRemoteAddr());
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/index.jsp");
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

        doGet(request, response);
    }
}
