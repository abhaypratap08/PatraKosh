package com.patrakosh.api.auth;

import com.patrakosh.api.config.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionCookieService sessionCookieService;
    private final RequestRateLimiter requestRateLimiter;
    private final int loginMaxAttempts;
    private final long loginWindowSeconds;
    private final int signupMaxAttempts;
    private final long signupWindowSeconds;

    public AuthController(
            AuthService authService,
            SessionCookieService sessionCookieService,
            RequestRateLimiter requestRateLimiter,
            @org.springframework.beans.factory.annotation.Value("${patrakosh.auth.rate-limit.login.max-attempts:10}") int loginMaxAttempts,
            @org.springframework.beans.factory.annotation.Value("${patrakosh.auth.rate-limit.login.window-seconds:300}") long loginWindowSeconds,
            @org.springframework.beans.factory.annotation.Value("${patrakosh.auth.rate-limit.signup.max-attempts:5}") int signupMaxAttempts,
            @org.springframework.beans.factory.annotation.Value("${patrakosh.auth.rate-limit.signup.window-seconds:3600}") long signupWindowSeconds
    ) {
        this.authService = authService;
        this.sessionCookieService = sessionCookieService;
        this.requestRateLimiter = requestRateLimiter;
        this.loginMaxAttempts = loginMaxAttempts;
        this.loginWindowSeconds = loginWindowSeconds;
        this.signupMaxAttempts = signupMaxAttempts;
        this.signupWindowSeconds = signupWindowSeconds;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
        requestRateLimiter.check(
                "signup:" + clientIp(servletRequest),
                signupMaxAttempts,
                Duration.ofSeconds(signupWindowSeconds),
                "Too many signup attempts. Try again later."
        );

        AuthService.AuthSession session = authService.register(
                request.username(),
                request.email(),
                request.password(),
                request.confirmPassword()
        );
        return buildAuthenticatedResponse(session, servletRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String clientIp = clientIp(servletRequest);
        requestRateLimiter.check(
                "login:ip:" + clientIp,
                loginMaxAttempts,
                Duration.ofSeconds(loginWindowSeconds),
                "Too many login attempts. Try again later."
        );
        requestRateLimiter.check(
                "login:identity:" + clientIp + ":" + normalizeIdentifier(request.usernameOrEmail()),
                loginMaxAttempts,
                Duration.ofSeconds(loginWindowSeconds),
                "Too many login attempts. Try again later."
        );

        AuthService.AuthSession session = authService.login(request.usernameOrEmail(), request.password());
        return buildAuthenticatedResponse(session, servletRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            HttpServletRequest servletRequest
    ) {
        authService.logout(authorizationHeader);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookieService.clearSessionCookie(servletRequest).toString())
                .build();
    }

    @GetMapping("/me")
    public AuthService.UserSummary me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        return authService.currentUser(authorizationHeader);
    }

    private ResponseEntity<AuthResponse> buildAuthenticatedResponse(AuthService.AuthSession session, HttpServletRequest request) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        sessionCookieService.createSessionCookie(
                                session.token(),
                                Duration.ofSeconds(authService.getSessionTtlSeconds()),
                                request
                        ).toString()
                )
                .body(new AuthResponse(session.user()));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int commaIndex = forwardedFor.indexOf(',');
            return (commaIndex >= 0 ? forwardedFor.substring(0, commaIndex) : forwardedFor).trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private static String normalizeIdentifier(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record SignupRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") String email,
            @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,
            @NotBlank(message = "Confirm password is required") String confirmPassword
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Username or email is required") String usernameOrEmail,
            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record AuthResponse(AuthService.UserSummary user) {
    }
}
