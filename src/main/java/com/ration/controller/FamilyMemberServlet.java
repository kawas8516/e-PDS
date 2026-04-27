package com.ration.controller;

import com.ration.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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

        Integer userId = getLoggedInUserId(request.getSession(false));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        Date parsedDob = parseDate(dob);
        if (parsedDob == null) {
            response.sendRedirect(request.getContextPath() + "/family.html?error=invalid_dob");
            return;
        }

        boolean saved = saveFamilyMember(userId, memberName, relation, aadhaar, parsedDob, gender);
        if (!saved) {
            response.sendRedirect(request.getContextPath() + "/family.html?error=db_insert_failed");
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

    private boolean saveFamilyMember(int userId, String memberName, String relation, String aadhaar, Date dob, String gender) {
        String sql = "INSERT INTO family_members (user_id, member_name, relation, aadhaar, dob, gender) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, memberName);
            statement.setString(3, relation);
            statement.setString(4, aadhaar);
            statement.setDate(5, dob);
            statement.setString(6, gender);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[FamilyMemberServlet] saveFamilyMember failed: " + e.getMessage());
            return false;
        }
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

    private Date parseDate(String dob) {
        try {
            return Date.valueOf(LocalDate.parse(dob));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
