package com.patrakosh.controller;

import com.patrakosh.MainApp;
import com.patrakosh.model.User;
import com.patrakosh.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signupLink;
    
    private AuthService authService;
    private boolean isPasswordVisible = false;
    
    @FXML
    public void initialize() {
        authService = new AuthService();
        
        // Bind text fields for password visibility toggle
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Add enter key handler
        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> handleLogin());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Clear previous error
        errorLabel.setVisible(false);
        
        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        // Disable button during login
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        
        // Perform login in background thread
        new Thread(() -> {
            try {
                User user = authService.login(username, password);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    MainApp.setCurrentUser(user);
                    MainApp.showDashboard();
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    loginButton.setDisable(false);
                    loginButton.setText("Login");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleSignupLink() {
        MainApp.showSignup();
    }
    
    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordBtn.setText("👁");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
