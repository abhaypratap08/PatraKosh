package com.patrakosh.controller;

import com.patrakosh.MainApp;
import com.patrakosh.api.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label usernameError;

    @FXML
    private Label emailError;

    @FXML
    private Label passwordError;

    @FXML
    private Label confirmPasswordError;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private void initialize() {
        setFieldError(usernameError, null);
        setFieldError(emailError, null);
        setFieldError(passwordError, null);
        setFieldError(confirmPasswordError, null);
        setMessage(errorLabel, null);
        setMessage(successLabel, null);
    }

    @FXML
    private void handleSignup() {
        String username = textValue(usernameField);
        String email = textValue(emailField);
        String password = rawValue(passwordField);
        String confirmPassword = rawValue(confirmPasswordField);

        setFieldError(usernameError, username.isBlank() ? "Username is required." : null);
        setFieldError(emailError, isValidEmail(email) ? null : "Enter a valid email address.");
        setFieldError(passwordError, password.length() >= 6 ? null : "Password must be at least 6 characters.");
        setFieldError(confirmPasswordError, password.equals(confirmPassword) ? null : "Passwords do not match.");

        boolean valid = !username.isBlank()
                && isValidEmail(email)
                && password.length() >= 6
                && password.equals(confirmPassword);

        if (!valid) {
            setMessage(errorLabel, "Fix the highlighted fields before continuing.");
            setMessage(successLabel, null);
            return;
        }

        try {
            MainApp.setCurrentUser(MainApp.getDesktopAuthService().register(username, email, password, confirmPassword));
            setMessage(errorLabel, null);
            setMessage(successLabel, "Account created. Redirecting to your dashboard...");
            MainApp.showDashboard();
        } catch (ValidationException exception) {
            setFieldError(usernameError, exception.getFieldErrors().get("username"));
            setFieldError(emailError, exception.getFieldErrors().get("email"));
            setFieldError(passwordError, exception.getFieldErrors().get("password"));
            setFieldError(confirmPasswordError, exception.getFieldErrors().get("confirmPassword"));
            setMessage(errorLabel, exception.getMessage());
            setMessage(successLabel, null);
        }
    }

    @FXML
    private void handleLoginLink() {
        MainApp.showLogin();
    }

    private static String textValue(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private static String rawValue(TextField field) {
        return field.getText() == null ? "" : field.getText();
    }

    private static boolean isValidEmail(String email) {
        return email.contains("@") && email.indexOf('@') > 0 && email.indexOf('@') < email.length() - 1;
    }

    private static void setFieldError(Label label, String message) {
        boolean hasMessage = message != null && !message.isBlank();
        label.setText(hasMessage ? message : "");
        label.setVisible(hasMessage);
        label.setManaged(hasMessage);
    }

    private static void setMessage(Label label, String message) {
        boolean hasMessage = message != null && !message.isBlank();
        label.setText(hasMessage ? message : "");
        label.setVisible(hasMessage);
        label.setManaged(hasMessage);
    }
}
