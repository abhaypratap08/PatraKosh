package com.patrakosh;

import com.patrakosh.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    
    private static Stage primaryStage;
    private static User currentUser;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("PatraKosh - File Storage");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        
        // Set application icon (optional)
        try {
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        // Show login screen
        showLogin();
        
        primaryStage.show();
    }
    
    public static void showLogin() {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("PatraKosh - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void showSignup() {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/signup.fxml"));
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("PatraKosh - Sign Up");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void showDashboard() {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            primaryStage.setScene(scene);
            primaryStage.setTitle("PatraKosh - Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
