package com.patrakosh.cache;

import com.patrakosh.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user sessions using concurrent collections.
 * Demonstrates proper use of ConcurrentHashMap for thread-safe session management.
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    
    // Map of session ID to User
    private final Map<String, User> activeSessions;
    
    // Map of user ID to set of session IDs (for tracking multiple sessions per user)
    private final Map<Integer, Set<String>> userSessions;
    
    private SessionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.userSessions = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets the singleton instance of SessionManager.
     *
     * @return the SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Creates a new session for a user.
     *
     * @param user the user
     * @return the session ID
     */
    public String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        
        activeSessions.put(sessionId, user);
        
        // Track session for user
        userSessions.computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
        
        logger.info("Session created for user: {} with session ID: {}", user.getUsername(), sessionId);
        return sessionId;
    }
    
    /**
     * Gets a user by session ID.
     *
     * @param sessionId the session ID
     * @return the user, or null if session doesn't exist
     */
    public User getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * Checks if a session is valid.
     *
     * @param sessionId the session ID
     * @return true if valid, false otherwise
     */
    public boolean isValidSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }
    
    /**
     * Invalidates a specific session.
     *
     * @param sessionId the session ID
     */
    public void invalidateSession(String sessionId) {
        User user = activeSessions.remove(sessionId);
        
        if (user != null) {
            Set<String> sessions = userSessions.get(user.getId());
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(user.getId());
                }
            }
            logger.info("Session invalidated for user: {}", user.getUsername());
        }
    }
    
    /**
     * Invalidates all sessions for a user.
     *
     * @param userId the user ID
     */
    public void invalidateUserSessions(Integer userId) {
        Set<String> sessions = userSessions.remove(userId);
        
        if (sessions != null) {
            for (String sessionId : sessions) {
                activeSessions.remove(sessionId);
            }
            logger.info("All sessions invalidated for user ID: {}", userId);
        }
    }
    
    /**
     * Gets all active session IDs for a user.
     *
     * @param userId the user ID
     * @return set of session IDs
     */
    public Set<String> getUserSessions(Integer userId) {
        return userSessions.getOrDefault(userId, Set.of());
    }
    
    /**
     * Gets the count of active sessions.
     *
     * @return number of active sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Gets the count of active users (users with at least one session).
     *
     * @return number of active users
     */
    public int getActiveUserCount() {
        return userSessions.size();
    }
    
    /**
     * Clears all sessions (use with caution).
     */
    public void clearAllSessions() {
        activeSessions.clear();
        userSessions.clear();
        logger.warn("All sessions cleared");
    }
}
