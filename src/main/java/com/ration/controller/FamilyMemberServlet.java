package com.ration.controller;

import com.ration.dao.CitizenDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/FamilyMemberServlet")
public class FamilyMemberServlet extends HttpServlet {

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
        List<Map<String, Object>> members = citizenDAO.getFamilyMembersByUser(userId);

        long pending  = members.stream().filter(m -> m.get("aadhaar") == null).count();
        long verified = members.size() - pending;

        request.setAttribute("members", members);
        request.setAttribute("totalCount", members.size());
        request.setAttribute("verifiedCount", verified);
        request.setAttribute("pendingCount", pending);

        String success = request.getParameter("success");
        if ("1".equals(success)) request.setAttribute("successMsg", "Family member added successfully.");

        request.getRequestDispatcher("/WEB-INF/views/family.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        int userId     = (Integer) session.getAttribute("userId");
        String name    = trim(request.getParameter("memberName"));
        String relation = trim(request.getParameter("relation"));
        String aadhaar = trim(request.getParameter("aadhaar"));
        String dob     = trim(request.getParameter("dob"));
        String gender  = trim(request.getParameter("gender"));

        if (isBlank(name) || isBlank(relation) || isBlank(aadhaar) || isBlank(dob) || isBlank(gender)) {
            response.sendRedirect(request.getContextPath() + "/FamilyMemberServlet?error=missing_fields");
            return;
        }

        System.out.printf("[FamilyMemberServlet] userId=%d name=%s relation=%s dob=%s gender=%s aadhaarSuffix=%s%n",
                userId, name, relation, dob, gender,
                aadhaar.length() >= 4 ? aadhaar.substring(aadhaar.length() - 4) : aadhaar);

        try {
            int cardId = citizenDAO.getOrCreateCardId(userId);
            boolean ok = citizenDAO.insertFamilyMember(cardId, name, relation, aadhaar, dob, gender);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/FamilyMemberServlet?success=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/FamilyMemberServlet?error=db_error");
            }
        } catch (SQLException e) {
            System.err.println("[FamilyMemberServlet] DB error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/FamilyMemberServlet?error=db_error");
        }
    }

    private String trim(String v) { return v == null ? null : v.trim(); }
    private boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
}
