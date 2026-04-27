package com.ration.controller;

import com.ration.dao.ComplaintDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/ComplaintServlet")
public class ComplaintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ComplaintDAO complaintDAO;

    @Override
    public void init() throws ServletException {
        complaintDAO = new ComplaintDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/complaints.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String complaintType = trim(request.getParameter("type"));
        String month = trim(request.getParameter("month"));
        String description = trim(request.getParameter("description"));

        if (isBlank(complaintType) || isBlank(description)) {
            response.sendRedirect(request.getContextPath() + "/complaints.html?error=missing_fields");
            return;
        }

        Integer userId = getLoggedInUserId(request.getSession(false));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        boolean saved = complaintDAO.createComplaint(userId, complaintType, month, description);
        if (!saved) {
            response.sendRedirect(request.getContextPath() + "/complaints.html?error=db_insert_failed");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/complaints.html?success=1");
    }

    private Integer getLoggedInUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer) {
            return (Integer) userId;
        }
        return null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
