package com.ration.controller;

import com.ration.dao.CitizenDAO;
import com.ration.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/AdminFamilyServlet")
public class AdminFamilyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private CitizenDAO citizenDAO;

    @Override
    public void init() throws ServletException {
        citizenDAO = new CitizenDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Map<String, Object>> families = citizenDAO.getAllFamilies();

        // If a specific family is selected, load their members too
        String cardIdParam = request.getParameter("cardId");
        if (cardIdParam != null) {
            try {
                int cardId = Integer.parseInt(cardIdParam);
                List<Map<String, Object>> members = citizenDAO.getFamilyMembersByCard(cardId);
                request.setAttribute("selectedCardId", cardId);
                request.setAttribute("selectedMembers", members);
            } catch (NumberFormatException ignored) { }
        }

        request.setAttribute("families", families);

        String msg = request.getParameter("msg");
        if (msg != null) request.setAttribute("msg", msg);

        request.getRequestDispatcher("/WEB-INF/views/admin-family.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");

        if ("updateCardType".equals(action)) {
            int cardId   = Integer.parseInt(request.getParameter("cardId"));
            String type  = request.getParameter("cardType");
            if ("BPL".equals(type) || "APL".equals(type) || "AAY".equals(type)) {
                citizenDAO.updateCardType(cardId, type);
            }
            response.sendRedirect(request.getContextPath()
                + "/AdminFamilyServlet?msg=card_updated&cardId=" + cardId);

        } else if ("updateIncome".equals(action)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            long income = 0;
            try { income = Long.parseLong(request.getParameter("annualIncome").replaceAll("[^0-9]", "")); }
            catch (NumberFormatException ignored) { }
            citizenDAO.updateFamilyIncome(userId, income);
            response.sendRedirect(request.getContextPath()
                + "/AdminFamilyServlet?msg=income_updated");

        } else if ("deleteMember".equals(action)) {
            int memberId = Integer.parseInt(request.getParameter("memberId"));
            int cardId   = Integer.parseInt(request.getParameter("cardId"));
            citizenDAO.deleteFamilyMember(memberId);
            response.sendRedirect(request.getContextPath()
                + "/AdminFamilyServlet?msg=member_deleted&cardId=" + cardId);

        } else {
            response.sendRedirect(request.getContextPath() + "/AdminFamilyServlet");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }
}
