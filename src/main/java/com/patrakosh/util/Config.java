package com.patrakosh.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = Config.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Unable to find application.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    public static String getDbUrl() {
        return get("db.url");
    }
    
    public static String getDbUsername() {
        return get("db.username");
    }
    
    public static String getDbPassword() {
        return get("db.password");
    }
    
    public static String getDbDriver() {
        return get("db.driver");
    }
    
    public static String getStorageBasePath() {
        return get("storage.base.path");
    }
}
