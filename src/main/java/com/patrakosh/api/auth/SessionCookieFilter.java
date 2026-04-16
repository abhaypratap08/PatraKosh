package com.patrakosh.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

@Component
public class SessionCookieFilter extends OncePerRequestFilter {

    private final SessionCookieService sessionCookieService;

    public SessionCookieFilter(SessionCookieService sessionCookieService) {
        this.sessionCookieService = sessionCookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String cookieToken = sessionCookieService.extractToken(request.getCookies());
        if (cookieToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                    return "Bearer " + cookieToken;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                    return Collections.enumeration(Collections.singletonList("Bearer " + cookieToken));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> headerNames = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
                headerNames.add(HttpHeaders.AUTHORIZATION);
                return new Vector<>(headerNames).elements();
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }
}
