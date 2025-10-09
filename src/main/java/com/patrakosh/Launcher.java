package com.patrakosh;

/**
 * Launcher class to avoid JavaFX module issues when creating executable JAR
 * This class does not extend Application and simply calls the main method of MainApp
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
