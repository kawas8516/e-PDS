package com.ration.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/FamilyMemberServlet")
public class FamilyMemberServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/family.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String memberName = trim(request.getParameter("memberName"));
        String relation = trim(request.getParameter("relation"));
        String aadhaar = trim(request.getParameter("aadhaar"));
        String dob = trim(request.getParameter("dob"));
        String gender = trim(request.getParameter("gender"));

        if (isBlank(memberName) || isBlank(relation) || isBlank(aadhaar) || isBlank(dob) || isBlank(gender)) {
            response.sendRedirect(request.getContextPath() + "/family.html?error=missing_fields");
            return;
        }

        System.out.printf("[FamilyMemberServlet] name=%s relation=%s dob=%s gender=%s aadhaarSuffix=%s%n",
                memberName,
                relation,
                dob,
                gender,
                aadhaar.length() >= 4 ? aadhaar.substring(aadhaar.length() - 4) : aadhaar);

        response.sendRedirect(request.getContextPath() + "/family.html?success=1");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
