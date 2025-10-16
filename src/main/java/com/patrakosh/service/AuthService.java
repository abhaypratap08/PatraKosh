package com.patrakosh.service;

import com.patrakosh.dao.ActivityLogDAO;
import com.patrakosh.dao.UserDAO;
import com.patrakosh.exception.*;
import com.patrakosh.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Authentication service handling user login and signup operations.
 * Extends BaseService to inherit logging and transaction management capabilities.
 */
public class AuthService extends BaseService {
    private final UserDAO userDAO;
    private final ActivityLogDAO activityLogDAO;
    
    public AuthService() {
        super();
        this.userDAO = new UserDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }
    
    /**
     * Authenticates a user with username/email and password.
     *
     * @param usernameOrEmail the username or email
     * @param password the password
     * @return the authenticated user
     * @throws AuthenticationException if authentication fails
     */
    public User login(String usernameOrEmail, String password) throws AuthenticationException {
        logOperation("login", usernameOrEmail);
        
        try {
            try {
                validateInput(usernameOrEmail);
            } catch (ValidationException e) {
                throw new AuthenticationException("Invalid input", e);
            }
            
            User user;
            
            // Try to find user by email or username
            if (usernameOrEmail.contains("@")) {
                user = userDAO.getUserByEmail(usernameOrEmail);
            } else {
                user = userDAO.getUserByUsername(usernameOrEmail);
            }
            
            if (user == null) {
                throw new UserNotFoundException("User not found: " + usernameOrEmail);
            }
            
            String hashedPassword = hashPassword(password);
            if (!user.getPassword().equals(hashedPassword)) {
                throw new InvalidCredentialsException("Invalid password for user: " + usernameOrEmail);
            }
            
            // Log successful login
            try {
                activityLogDAO.logActivity(user.getId(), "LOGIN", "USER", user.getId());
            } catch (DatabaseException e) {
                logger.warn("Failed to log login activity", e);
            }
            
            logger.info("User logged in successfully: {}", usernameOrEmail);
            return user;
            
        } catch (DatabaseException e) {
            logError("login", e);
            throw new AuthenticationException("Login failed due to database error", e);
        }
    }
    
    /**
     * Registers a new user account.
     *
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param confirmPassword the password confirmation
     * @return the created user
     * @throws ValidationException if validation fails
     * @throws AuthenticationException if user creation fails
     */
    public User signup(String username, String email, String password, String confirmPassword) 
            throws ValidationException, AuthenticationException {
        logOperation("signup", username, email);
        
        try {
            // Validate inputs
            validateSignupInput(username, email, password, confirmPassword);
            
            // Check if username already exists
            if (userDAO.isUsernameExists(username)) {
                throw new ValidationException("Username already exists: " + username);
            }
            
            // Check if email already exists
            if (userDAO.isEmailExists(email)) {
                throw new ValidationException("Email already exists: " + email);
            }
            
            // Create new user with transaction
            User newUser = transactionManager.executeInTransaction(conn -> {
                String hashedPassword = hashPassword(password);
                User user = new User(username, email, hashedPassword);
                
                // Save user
                User savedUser = userDAO.save(user);
                
                // Log signup activity
                activityLogDAO.logActivity(savedUser.getId(), "SIGNUP", "USER", savedUser.getId(), 
                    "New user registered");
                
                return savedUser;
            });
            
            logger.info("User signed up successfully: {}", username);
            return newUser;
            
        } catch (ValidationException e) {
            logError("signup", e);
            throw e;
        } catch (Exception e) {
            logError("signup", e);
            throw new AuthenticationException("Signup failed", e);
        }
    }
    
    /**
     * Validates signup input data.
     *
     * @param username the username
     * @param email the email
     * @param password the password
     * @param confirmPassword the password confirmation
     * @throws ValidationException if validation fails
     */
    private void validateSignupInput(String username, String email, String password, String confirmPassword) 
            throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        
        if (username.length() < 3) {
            throw new ValidationException("Username must be at least 3 characters");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        
        if (!isValidEmail(email)) {
            throw new InvalidEmailException("Invalid email format: " + email);
        }
        
        if (password == null || password.length() < 6) {
            throw new InvalidPasswordException("Password must be at least 6 characters");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
    }
    
    @Override
    protected void validateInput(Object input) throws ValidationException {
        if (input == null) {
            throw new ValidationException("Input cannot be null");
        }
        
        if (input instanceof String) {
            String str = (String) input;
            if (str.trim().isEmpty()) {
                throw new ValidationException("Input cannot be empty");
            }
        }
    }
    
    /**
     * Hashes a password using SHA-256.
     *
     * @param password the plain text password
     * @return the hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Validates email format.
     *
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validates password strength.
     *
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }
}
