package com.patrakosh.controller;

import com.patrakosh.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        setError("");
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean showPlainText = !passwordTextField.isVisible();
        passwordTextField.setVisible(showPlainText);
        passwordTextField.setManaged(showPlainText);
        passwordField.setVisible(!showPlainText);
        passwordField.setManaged(!showPlainText);
        togglePasswordBtn.setText(showPlainText ? "🙈" : "👁");
    }

    @FXML
    private void handleLogin() {
        String identity = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        try {
            MainApp.setCurrentUser(MainApp.getDesktopAuthService().login(identity, password));
            setError("");
            MainApp.showDashboard();
        } catch (IllegalArgumentException exception) {
            setError(exception.getMessage());
        }
    }

    @FXML
    private void handleSignupLink() {
        MainApp.showSignup();
    }

    private void setError(String message) {
        boolean hasMessage = message != null && !message.isBlank();
        errorLabel.setText(hasMessage ? message : "");
        errorLabel.setVisible(hasMessage);
        errorLabel.setManaged(hasMessage);
    }
}
