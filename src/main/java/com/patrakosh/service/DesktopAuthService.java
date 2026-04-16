package com.patrakosh.service;

import com.patrakosh.api.ValidationException;
import com.patrakosh.model.User;
import com.patrakosh.persistence.AppStateStore;
import com.patrakosh.security.PasswordHasher;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class DesktopAuthService {

    private final AppStateStore stateStore;

    public DesktopAuthService(Path dataRoot) {
        this.stateStore = new AppStateStore(dataRoot);
    }

    public User login(String usernameOrEmail, String password) {
        String identifier = normalize(usernameOrEmail);
        if (identifier.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Enter both your username/email and password.");
        }

        return stateStore.read(state -> state.users.stream()
                .filter(user -> normalize(user.username).equals(identifier) || normalize(user.email).equals(identifier))
                .findFirst()
                .filter(user -> PasswordHasher.matches(password, user.passwordHash))
                .map(this::toUser)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/email or password.")));
    }

    public User register(String username, String email, String password, String confirmPassword) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);

        if (normalizedUsername.isBlank()) {
            fieldErrors.put("username", "Username is required.");
        }
        if (!isValidEmail(email)) {
            fieldErrors.put("email", "Enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            fieldErrors.put("password", "Password must be at least 6 characters.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            fieldErrors.put("confirmPassword", "Passwords do not match.");
        }

        User user = stateStore.write(state -> {
            if (state.users.stream().anyMatch(existing -> normalize(existing.username).equals(normalizedUsername))) {
                fieldErrors.put("username", "This username is already taken.");
            }
            if (state.users.stream().anyMatch(existing -> normalize(existing.email).equals(normalizedEmail))) {
                fieldErrors.put("email", "An account with this email already exists.");
            }

            if (!fieldErrors.isEmpty()) {
                throw new ValidationException("Fix the highlighted fields before continuing.", fieldErrors);
            }

            AppStateStore.UserRecord record = new AppStateStore.UserRecord(
                    state.nextUserId++,
                    username.trim(),
                    email.trim(),
                    PasswordHasher.hash(password),
                    Instant.now()
            );
            state.users.add(record);
            return toUser(record);
        });

        return user;
    }

    private User toUser(AppStateStore.UserRecord record) {
        return new User(record.id, record.username, record.email, record.createdAt);
    }

    private static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        return atIndex > 0 && atIndex < trimmed.length() - 1;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
