package com.patrakosh.service;

import com.patrakosh.dao.UserDAO;
import com.patrakosh.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;

public class AuthService {
    private final UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    public User login(String usernameOrEmail, String password) throws SQLException {
        User user = null;
        
        if (usernameOrEmail.contains("@")) {
            user = userDAO.getUserByEmail(usernameOrEmail);
        } else {
            user = userDAO.getUserByUsername(usernameOrEmail);
        }
        
        if (user == null) {
            throw new SQLException("User not found");
        }
        
        String hashedPassword = hashPassword(password);
        if (!user.getPassword().equals(hashedPassword)) {
            throw new SQLException("Invalid password");
        }
        
        return user;
    }
    
    public User signup(String username, String email, String password, String confirmPassword) throws SQLException {
       
        if (username == null || username.trim().isEmpty()) {
            throw new SQLException("Username cannot be empty");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new SQLException("Email cannot be empty");
        }
        
        if (!isValidEmail(email)) {
            throw new SQLException("Invalid email format");
        }
        
        if (password == null || password.length() < 6) {
            throw new SQLException("Password must be at least 6 characters");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new SQLException("Passwords do not match");
        }
        
        // Checks if username already exists
        if (userDAO.isUsernameExists(username)) {
            throw new SQLException("Username already exists");
        }
        
        // Checking if the email already exists
        if (userDAO.isEmailExists(email)) {
            throw new SQLException("Email already exists");
        }
        
        String hashedPassword = hashPassword(password);
        User newUser = new User(username, email, hashedPassword);
        
        return userDAO.createUser(newUser);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    public boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }
}
