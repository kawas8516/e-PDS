package com.ration.service;

import com.ration.dao.UserDAO;
import com.ration.model.User;

public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 100;

    private final UserDAO userDAO;

    public AuthService() {
        this(new UserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User authenticate(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return null;
        }

        String normalizedUsername = username.trim();
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return null;
        }

        return userDAO.authenticate(normalizedUsername, password);
    }

    public boolean validateUserRole(User user, String role) {
        if (user == null || isBlank(user.getRole())) {
            return false;
        }
        return user.getRole().equalsIgnoreCase(role);
    }

    public String getDashboardPath(User user) {
        return validateUserRole(user, "admin") ? "/admin-dashboard.html" : "/citizen-dashboard.html";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
