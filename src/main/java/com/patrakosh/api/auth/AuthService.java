package com.patrakosh.api.auth;

import com.patrakosh.api.ValidationException;
import com.patrakosh.api.activity.ActivityService;
import com.patrakosh.persistence.AppStateStore;
import com.patrakosh.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {

    private final AppStateStore stateStore;
    private final ActivityService activityService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long sessionTtlSeconds;

    public AuthService(
            AppStateStore stateStore,
            ActivityService activityService,
            @Value("${patrakosh.auth.session-ttl-seconds:43200}") long sessionTtlSeconds
    ) {
        this.stateStore = stateStore;
        this.activityService = activityService;
        this.sessionTtlSeconds = sessionTtlSeconds;
    }

    public AuthSession register(String username, String email, String password, String confirmPassword) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);

        if (!password.equals(confirmPassword)) {
            fieldErrors.put("confirmPassword", "Passwords do not match.");
        }

        AuthSession session = stateStore.write(state -> {
            if (state.users.stream().anyMatch(user -> normalize(user.username).equals(normalizedUsername))) {
                fieldErrors.put("username", "This username is already taken.");
            }
            if (state.users.stream().anyMatch(user -> normalize(user.email).equals(normalizedEmail))) {
                fieldErrors.put("email", "An account with this email already exists.");
            }

            if (!fieldErrors.isEmpty()) {
                throw new ValidationException("Validation failed", fieldErrors);
            }

            long userId = state.nextUserId++;
            AppStateStore.UserRecord userRecord = new AppStateStore.UserRecord(
                    userId,
                    username.trim(),
                    email.trim(),
                    PasswordHasher.hash(password),
                    Instant.now()
            );
            state.users.add(userRecord);

            AppStateStore.SessionRecord sessionRecord = newSessionRecord(userId);
            state.sessions.add(sessionRecord);

            return new AuthSession(sessionRecord.token, UserSummary.from(userRecord));
        });

        activityService.record(session.user().id(), "SIGNUP", null);
        return session;
    }

    public AuthSession login(String usernameOrEmail, String password) {
        AuthSession session = stateStore.write(state -> {
            Instant now = Instant.now();
            state.sessions.removeIf(existing -> isExpiredOrRevoked(existing, now));

            String normalizedIdentifier = normalize(usernameOrEmail);
            AppStateStore.UserRecord userRecord = state.users.stream()
                    .filter(user -> normalize(user.username).equals(normalizedIdentifier)
                            || normalize(user.email).equals(normalizedIdentifier))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password"));

            if (!PasswordHasher.matches(password, userRecord.passwordHash)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
            }

            AppStateStore.SessionRecord sessionRecord = newSessionRecord(userRecord.id);
            state.sessions.add(sessionRecord);
            return new AuthSession(sessionRecord.token, UserSummary.from(userRecord));
        });

        activityService.record(session.user().id(), "LOGIN", null);
        return session;
    }

    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader, false);
        if (token == null) {
            return;
        }

        UserAccount user = stateStore.write(state -> {
            Instant now = Instant.now();
            AppStateStore.SessionRecord sessionRecord = state.sessions.stream()
                    .filter(session -> token.equals(session.token))
                    .findFirst()
                    .orElse(null);

            if (sessionRecord == null || isExpiredOrRevoked(sessionRecord, now)) {
                return null;
            }

            sessionRecord.revokedAt = now;
            AppStateStore.UserRecord userRecord = findUser(state, sessionRecord.userId);
            return UserAccount.from(userRecord);
        });

        if (user != null) {
            activityService.record(user.id(), "LOGOUT", null);
        }
    }

    public UserSummary currentUser(String authorizationHeader) {
        return UserSummary.from(requireUserRecord(authorizationHeader));
    }

    public UserAccount requireUser(String authorizationHeader) {
        return UserAccount.from(requireUserRecord(authorizationHeader));
    }

    public long getSessionTtlSeconds() {
        return sessionTtlSeconds;
    }

    private AppStateStore.UserRecord requireUserRecord(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader, true);

        return stateStore.write(state -> {
            Instant now = Instant.now();
            state.sessions.removeIf(session -> isExpiredOrRevoked(session, now));

            AppStateStore.SessionRecord sessionRecord = state.sessions.stream()
                    .filter(session -> token.equals(session.token))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));

            return findUser(state, sessionRecord.userId);
        });
    }

    private static AppStateStore.UserRecord findUser(AppStateStore.StateSnapshot state, long userId) {
        return state.users.stream()
                .filter(user -> user.id == userId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session is no longer valid"));
    }

    private AppStateStore.SessionRecord newSessionRecord(long userId) {
        Instant now = Instant.now();
        return new AppStateStore.SessionRecord(
                randomToken(),
                userId,
                now,
                now.plusSeconds(sessionTtlSeconds),
                null
        );
    }

    private String randomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private static boolean isExpiredOrRevoked(AppStateStore.SessionRecord sessionRecord, Instant now) {
        return sessionRecord.revokedAt != null
                || sessionRecord.expiresAt == null
                || !sessionRecord.expiresAt.isAfter(now);
    }

    private static String extractBearerToken(String authorizationHeader, boolean required) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is required");
            }
            return null;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is required");
            }
            return null;
        }
        return token;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record UserAccount(long id, String username, String email, Instant createdAt) {
        private static UserAccount from(AppStateStore.UserRecord userRecord) {
            return new UserAccount(userRecord.id, userRecord.username, userRecord.email, userRecord.createdAt);
        }
    }

    public record UserSummary(long id, String username, String email, Instant createdAt) {
        private static UserSummary from(AppStateStore.UserRecord userRecord) {
            return new UserSummary(userRecord.id, userRecord.username, userRecord.email, userRecord.createdAt);
        }

        private static UserSummary from(UserAccount user) {
            return new UserSummary(user.id, user.username, user.email, user.createdAt);
        }
    }

    public record AuthSession(String token, UserSummary user) {
    }
}
