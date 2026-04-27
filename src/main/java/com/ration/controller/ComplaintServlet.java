package com.ration.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/ComplaintServlet")
public class ComplaintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        System.out.printf("[ComplaintServlet] type=%s month=%s descriptionLength=%d%n",
                complaintType, month, description.length());

        response.sendRedirect(request.getContextPath() + "/complaints.html?success=1");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
