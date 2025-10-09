package com.patrakosh.controller;

import com.patrakosh.MainApp;
import com.patrakosh.model.User;
import com.patrakosh.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label usernameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button signupButton;
    @FXML private Hyperlink loginLink;
    
    private AuthService authService;
    
    @FXML
    public void initialize() {
        authService = new AuthService();
        
        // Add enter key handlers
        usernameField.setOnAction(event -> emailField.requestFocus());
        emailField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(event -> handleSignup());
    }
    
    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Clear previous errors
        clearErrors();
        
        // Validate inputs
        boolean hasError = false;
        
        if (username.isEmpty()) {
            showFieldError(usernameError, "Username is required");
            hasError = true;
        }
        
        if (email.isEmpty()) {
            showFieldError(emailError, "Email is required");
            hasError = true;
        }
        
        if (password.isEmpty()) {
            showFieldError(passwordError, "Password is required");
            hasError = true;
        } else if (password.length() < 6) {
            showFieldError(passwordError, "Password must be at least 6 characters");
            hasError = true;
        }
        
        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordError, "Please confirm your password");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordError, "Passwords do not match");
            hasError = true;
        }
        
        if (hasError) {
            return;
        }
        
        // Disable button during signup
        signupButton.setDisable(true);
        signupButton.setText("Creating account...");
        
        // Perform signup in background thread
        new Thread(() -> {
            try {
                User user = authService.signup(username, email, password, confirmPassword);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    showSuccess("Account created successfully! Redirecting to login...");
                    
                    // Redirect to login after 2 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> MainApp.showLogin());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    signupButton.setDisable(false);
                    signupButton.setText("Sign Up");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleLoginLink() {
        MainApp.showLogin();
    }
    
    private void clearErrors() {
        usernameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
    
    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
    }
}
