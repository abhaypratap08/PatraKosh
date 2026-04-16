package com.patrakosh.api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class SessionCookieService {

    private final String cookieName;
    private final String sameSite;
    private final boolean forceSecure;

    public SessionCookieService(
            @Value("${patrakosh.auth.cookie-name:PATRAKOSH_SESSION}") String cookieName,
            @Value("${patrakosh.auth.cookie-same-site:Strict}") String sameSite,
            @Value("${patrakosh.auth.cookie-force-secure:false}") boolean forceSecure
    ) {
        this.cookieName = cookieName;
        this.sameSite = sameSite;
        this.forceSecure = forceSecure;
    }

    public String cookieName() {
        return cookieName;
    }

    public ResponseCookie createSessionCookie(String token, Duration maxAge, HttpServletRequest request) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie clearSessionCookie(HttpServletRequest request) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String extractToken(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forceSecure || request.isSecure() || "https".equalsIgnoreCase(forwardedProto);
    }
}
