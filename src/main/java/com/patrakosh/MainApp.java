package com.patrakosh;

import com.patrakosh.model.User;
import com.patrakosh.service.DesktopAuthService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static User currentUser;
    private static final DesktopAuthService desktopAuthService = new DesktopAuthService(resolveDataPath());
    private static final Path desktopStorageBasePath = resolveDesktopStorageBasePath();

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("PatraKosh - File Storage");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        showLogin();
        primaryStage.show();
    }

    public static void showLogin() {
        showScene("/fxml/login.fxml", 900, 600, "PatraKosh - Login");
    }

    public static void showSignup() {
        showScene("/fxml/signup.fxml", 900, 600, "PatraKosh - Sign Up");
    }

    public static void showDashboard() {
        if (currentUser == null) {
            showLogin();
            return;
        }

        showScene("/fxml/dashboard.fxml", 1200, 700, "PatraKosh - Dashboard");
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static DesktopAuthService getDesktopAuthService() {
        return desktopAuthService;
    }

    public static Path getDesktopStorageBasePath() {
        return desktopStorageBasePath;
    }

    private static void showScene(String resourcePath, int width, int height, String title) {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource(resourcePath));
            primaryStage.setScene(new Scene(root, width, height));
            primaryStage.setTitle(title);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load scene " + resourcePath, exception);
        }
    }

    private static Path resolveDataPath() {
        String configuredPath = firstNonBlank(
                System.getProperty("patrakosh.data.base-path"),
                System.getenv("PATRAKOSH_DATA_BASE_PATH")
        );
        return Path.of(configuredPath == null ? "data" : configuredPath).toAbsolutePath().normalize();
    }

    private static Path resolveDesktopStorageBasePath() {
        String configuredPath = firstNonBlank(
                System.getProperty("patrakosh.desktop.storage.base-path"),
                System.getenv("PATRAKOSH_DESKTOP_STORAGE_BASE_PATH")
        );

        if (configuredPath != null) {
            return Path.of(configuredPath).toAbsolutePath().normalize();
        }

        return Path.of(System.getProperty("user.home"), ".patrakosh-desktop-storage")
                .toAbsolutePath()
                .normalize();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
