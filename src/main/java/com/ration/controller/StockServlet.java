package com.ration.controller;

import com.ration.dao.StockDAO;
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

@WebServlet("/StockServlet")
public class StockServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private StockDAO stockDAO;

    @Override
    public void init() throws ServletException {
        stockDAO = new StockDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Map<String, Object>> stocks = stockDAO.getAllStock();

        // Calculate low-stock items
        long lowStockCount = stocks.stream()
            .filter(s -> {
                int qty = s.get("quantity") != null ? (Integer) s.get("quantity") : 0;
                int threshold = s.get("thresholdLimit") != null ? (Integer) s.get("thresholdLimit") : 0;
                return threshold > 0 && qty <= threshold;
            })
            .count();

        request.setAttribute("stocks", stocks);
        request.setAttribute("totalItems", stocks.size());
        request.setAttribute("lowStockCount", lowStockCount);

        String msg = request.getParameter("msg");
        if (msg != null) request.setAttribute("msg", msg);

        request.getRequestDispatcher("/WEB-INF/views/stock.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            String itemName = trim(request.getParameter("itemName"));
            int quantity = parseInt(request.getParameter("quantity"), 0);
            double unitPrice = parseDouble(request.getParameter("unitPrice"), 0.0);
            int threshold = parseInt(request.getParameter("thresholdLimit"), 0);

            if (isBlank(itemName) || quantity <= 0) {
                response.sendRedirect(request.getContextPath() + "/StockServlet?msg=invalid_input");
                return;
            }

            boolean ok = stockDAO.addStock(itemName, quantity, unitPrice, threshold);
            response.sendRedirect(request.getContextPath() + "/StockServlet?msg=" + (ok ? "item_added" : "error"));

        } else if ("updateQty".equals(action)) {
            int itemId = parseInt(request.getParameter("itemId"), -1);
            int newQty = parseInt(request.getParameter("quantity"), -1);

            if (itemId <= 0 || newQty < 0) {
                response.sendRedirect(request.getContextPath() + "/StockServlet?msg=invalid_input");
                return;
            }

            boolean ok = stockDAO.updateQuantity(itemId, newQty);
            response.sendRedirect(request.getContextPath() + "/StockServlet?msg=" + (ok ? "qty_updated" : "error"));

        } else if ("updatePrice".equals(action)) {
            int itemId = parseInt(request.getParameter("itemId"), -1);
            double newPrice = parseDouble(request.getParameter("unitPrice"), -1);

            if (itemId <= 0 || newPrice < 0) {
                response.sendRedirect(request.getContextPath() + "/StockServlet?msg=invalid_input");
                return;
            }

            boolean ok = stockDAO.updatePrice(itemId, newPrice);
            response.sendRedirect(request.getContextPath() + "/StockServlet?msg=" + (ok ? "price_updated" : "error"));

        } else if ("updateThreshold".equals(action)) {
            int itemId = parseInt(request.getParameter("itemId"), -1);
            int threshold = parseInt(request.getParameter("thresholdLimit"), -1);

            if (itemId <= 0 || threshold < 0) {
                response.sendRedirect(request.getContextPath() + "/StockServlet?msg=invalid_input");
                return;
            }

            boolean ok = stockDAO.updateThreshold(itemId, threshold);
            response.sendRedirect(request.getContextPath() + "/StockServlet?msg=" + (ok ? "threshold_updated" : "error"));

        } else if ("delete".equals(action)) {
            int itemId = parseInt(request.getParameter("itemId"), -1);

            if (itemId <= 0) {
                response.sendRedirect(request.getContextPath() + "/StockServlet?msg=invalid_input");
                return;
            }

            boolean ok = stockDAO.deleteStock(itemId);
            response.sendRedirect(request.getContextPath() + "/StockServlet?msg=" + (ok ? "item_deleted" : "error"));

        } else {
            response.sendRedirect(request.getContextPath() + "/StockServlet");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }

    private String trim(String v) { return v == null ? null : v.trim(); }
    private boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
    private int parseInt(String v, int def) {
        try { return v != null ? Integer.parseInt(v.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }
    private double parseDouble(String v, double def) {
        try { return v != null ? Double.parseDouble(v.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }
}
