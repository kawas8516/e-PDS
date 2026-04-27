package com.ration.dao;

import com.ration.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FamilyMemberDAO {

    public boolean addFamilyMember(int userId,
                                   String memberName,
                                   String relation,
                                   String aadhaar,
                                   Date dob,
                                   String gender) {

        String[] sqlCandidates = {
                "INSERT INTO family_members (user_id, member_name, relation, aadhaar_number, dob, gender, created_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "INSERT INTO family_members (user_id, full_name, relationship, aadhaar, dob, gender, created_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "INSERT INTO family_members (user_id, member_name, relation, aadhaar_number, dob, gender) VALUES (?, ?, ?, ?, ?, ?)",
                "INSERT INTO family_members (user_id, full_name, relationship, aadhaar, dob, gender) VALUES (?, ?, ?, ?, ?, ?)"
        };

        for (String sql : sqlCandidates) {
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
                System.err.println("[FamilyMemberDAO] insert attempt failed: " + e.getMessage());
            }
        }

        return false;
    }
}
