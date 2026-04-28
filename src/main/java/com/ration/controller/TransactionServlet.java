package com.ration.controller;

import com.ration.dao.StockDAO;
import com.ration.dao.TransactionDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/TransactionServlet")
public class TransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private TransactionDAO transactionDAO;
    private StockDAO stockDAO;

    @Override
    public void init() throws ServletException {
        transactionDAO = new TransactionDAO();
        stockDAO = new StockDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        List<Map<String, Object>> activeCards = transactionDAO.getAllActiveCards();
        List<Map<String, Object>> stockItems = stockDAO.getAllStock();

        request.setAttribute("activeCards", activeCards);
        request.setAttribute("stockItems", stockItems);
        request.setAttribute("lowStockItems", stockDAO.getLowStockItems());

        request.getRequestDispatcher("/WEB-INF/views/transactions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        Integer cardId = parseInt(request.getParameter("cardId"));
        Integer itemId = parseInt(request.getParameter("itemId"));
        Integer quantity = parseInt(request.getParameter("quantity"));
        int issuedBy = (Integer) session.getAttribute("userId");

        if (cardId == null || itemId == null || quantity == null || quantity <= 0) {
            response.sendRedirect(request.getContextPath() + "/TransactionServlet?error=invalid_input");
            return;
        }

        // Verify selected stock item exists before issuing.
        Map<String, Object> stock = stockDAO.getStockById(itemId);
        if (stock == null) {
            response.sendRedirect(request.getContextPath() + "/TransactionServlet?error=item_not_found");
            return;
        }

        try {
            BigDecimal unitPrice = (BigDecimal) stock.get("unit_price");
            double amount = unitPrice.multiply(BigDecimal.valueOf(quantity)).doubleValue();

            boolean issued = transactionDAO.issueRation(cardId, itemId, quantity, amount, issuedBy);
            if (issued) {
                response.sendRedirect(request.getContextPath() + "/TransactionServlet?success=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/TransactionServlet?error=issue_failed");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/TransactionServlet?error=" + encodeError(e.getMessage()));
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String encodeError(String message) {
        if (message == null || message.isBlank()) return "db_error";
        return message.replaceAll("\\s+", "_");
    }
}
