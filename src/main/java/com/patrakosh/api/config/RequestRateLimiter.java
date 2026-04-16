package com.patrakosh.api.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RequestRateLimiter {

    private final ConcurrentHashMap<String, Deque<Long>> requestBuckets = new ConcurrentHashMap<>();

    public void check(String key, int maxRequests, Duration window, String message) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("maxRequests must be greater than zero");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }

        long now = System.currentTimeMillis();
        long threshold = now - window.toMillis();
        AtomicBoolean exceeded = new AtomicBoolean(false);
        requestBuckets.compute(key, (ignored, currentWindow) -> {
            Deque<Long> attempts = currentWindow == null ? new ArrayDeque<>() : currentWindow;
            while (!attempts.isEmpty() && attempts.peekFirst() < threshold) {
                attempts.pollFirst();
            }
            if (attempts.size() >= maxRequests) {
                exceeded.set(true);
                return attempts;
            }
            attempts.addLast(now);
            return attempts;
        });

        if (exceeded.get()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
        }
    }

    public void reset() {
        requestBuckets.clear();
    }
}
