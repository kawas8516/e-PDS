package com.ration.controller;

import com.ration.dao.CitizenDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/ComplaintServlet")
public class ComplaintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private CitizenDAO citizenDAO;

    @Override
    public void init() throws ServletException {
        citizenDAO = new CitizenDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        List<Map<String, Object>> complaints = citizenDAO.getComplaintsByUser(userId);

        long pending  = complaints.stream().filter(c -> "PENDING".equals(c.get("status"))).count();
        long resolved = complaints.stream().filter(c -> "RESOLVED".equals(c.get("status"))).count();

        request.setAttribute("complaints", complaints);
        request.setAttribute("totalCount", complaints.size());
        request.setAttribute("pendingCount", pending);
        request.setAttribute("resolvedCount", resolved);

        String success = request.getParameter("success");
        if ("1".equals(success)) request.setAttribute("successMsg", "Complaint filed successfully.");

        request.getRequestDispatcher("/WEB-INF/views/complaints.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String complaintType = trim(request.getParameter("type"));
        String month         = trim(request.getParameter("month"));
        String description   = trim(request.getParameter("description"));

        if (isBlank(complaintType) || isBlank(description)) {
            response.sendRedirect(request.getContextPath() + "/ComplaintServlet?error=missing_fields");
            return;
        }

        System.out.printf("[ComplaintServlet] userId=%d type=%s month=%s descLen=%d%n",
                userId, complaintType, month, description.length());

        boolean ok = citizenDAO.insertComplaint(userId, complaintType, month, description);
        if (ok) {
            response.sendRedirect(request.getContextPath() + "/ComplaintServlet?success=1");
        } else {
            response.sendRedirect(request.getContextPath() + "/ComplaintServlet?error=db_error");
        }
    }

    private String trim(String v) { return v == null ? null : v.trim(); }
    private boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
}
