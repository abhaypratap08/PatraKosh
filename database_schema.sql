-- ============================================================================
-- PatraKosh Database Schema - Complete Setup Script
-- ============================================================================
-- This script creates the complete database schema for PatraKosh
-- A secure file storage and sharing application
-- 
-- Features:
-- - User management with storage quotas
-- - File storage with versioning
-- - File sharing (public and private)
-- - Activity logging
-- - Session management
-- - Folder organization
-- ============================================================================

-- Create and use database
CREATE DATABASE IF NOT EXISTS patrakosh_db;
USE patrakosh_db;

-- ============================================================================
-- Drop existing tables (in correct order to handle foreign keys)
-- ============================================================================
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS file_shares;
DROP TABLE IF EXISTS file_versions;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS users;

-- ============================================================================
-- Users Table
-- ============================================================================
-- Stores user accounts with authentication and storage quota information
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'SHA-256 hashed password',
    storage_quota BIGINT DEFAULT 1073741824 COMMENT '1GB default quota in bytes',
    storage_used BIGINT DEFAULT 0 COMMENT 'Current storage usage in bytes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email)
) COMMENT='User accounts with authentication and storage management';

-- ============================================================================
-- Folders Table
-- ============================================================================
-- Organizes files into a hierarchical folder structure
CREATE TABLE folders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'Owner of the folder',
    parent_folder_id INT NULL COMMENT 'Parent folder for nested structure (NULL for root)',
    folder_name VARCHAR(255) NOT NULL COMMENT 'Name of the folder',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_folder_id) REFERENCES folders(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_parent_folder (parent_folder_id),
    UNIQUE KEY unique_user_folder (user_id, parent_folder_id, folder_name)
) COMMENT='Hierarchical folder structure for file organization';

-- ============================================================================
-- Files Table
-- ============================================================================
-- Stores file metadata with versioning and duplicate detection support
CREATE TABLE files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'Owner of the file',
    filename VARCHAR(255) NOT NULL,
    filepath VARCHAR(500) NOT NULL COMMENT 'Physical path on storage',
    file_size BIGINT NOT NULL COMMENT 'Size in bytes',
    file_hash VARCHAR(64) COMMENT 'SHA-256 hash for duplicate detection',
    mime_type VARCHAR(100) COMMENT 'File MIME type',
    version INT DEFAULT 1 COMMENT 'Current version number',
    folder_id INT NULL COMMENT 'Folder containing this file',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_upload_time (upload_time),
    INDEX idx_file_hash (file_hash),
    INDEX idx_folder_id (folder_id),
    INDEX idx_user_filename (user_id, filename),
    INDEX idx_mime_type (mime_type)
) COMMENT='File metadata with versioning and duplicate detection';

-- ============================================================================
-- File Shares Table
-- ============================================================================
-- Manages file sharing between users (private) and public link sharing
CREATE TABLE file_shares (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL COMMENT 'Reference to shared file',
    shared_by_user_id INT NOT NULL COMMENT 'User who shared the file',
    shared_with_user_id INT NULL COMMENT 'User receiving the share (NULL for public shares)',
    share_token VARCHAR(255) UNIQUE COMMENT 'Unique token for public link access',
    expires_at TIMESTAMP NULL COMMENT 'Share expiration timestamp (NULL for no expiration)',
    is_public BOOLEAN DEFAULT FALSE COMMENT 'Whether share is public or private',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (shared_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (shared_with_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_file_id (file_id),
    INDEX idx_shared_by (shared_by_user_id),
    INDEX idx_shared_with (shared_with_user_id),
    INDEX idx_share_token (share_token),
    INDEX idx_expires_at (expires_at)
) COMMENT='File sharing management (private and public)';

-- ============================================================================
-- Activity Logs Table
-- ============================================================================
-- Tracks all user activities for audit and monitoring
CREATE TABLE activity_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'User who performed the action',
    action VARCHAR(50) NOT NULL COMMENT 'Action type: UPLOAD, DOWNLOAD, DELETE, SHARE, LOGIN, LOGOUT, SIGNUP',
    resource_type VARCHAR(50) NOT NULL COMMENT 'Resource type: FILE, USER, SHARE, FOLDER',
    resource_id INT COMMENT 'ID of the affected resource',
    details TEXT COMMENT 'Additional details about the action',
    ip_address VARCHAR(45) COMMENT 'IP address of the user',
    user_agent TEXT COMMENT 'Browser/client user agent',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_action (user_id, action),
    INDEX idx_created_at (created_at),
    INDEX idx_resource (resource_type, resource_id)
) COMMENT='Audit log of all user activities';

-- ============================================================================
-- File Versions Table
-- ============================================================================
-- Stores file version history for rollback capability
CREATE TABLE file_versions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL COMMENT 'Reference to the main file',
    version_number INT NOT NULL COMMENT 'Version number',
    filepath VARCHAR(500) NOT NULL COMMENT 'Path to the version file',
    file_size BIGINT NOT NULL COMMENT 'Size of this version in bytes',
    file_hash VARCHAR(64) COMMENT 'SHA-256 hash of this version',
    change_description TEXT COMMENT 'Description of changes in this version',
    created_by INT COMMENT 'User who created this version',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY unique_file_version (file_id, version_number),
    INDEX idx_file_id (file_id),
    INDEX idx_created_at (created_at)
) COMMENT='File version history for rollback';

-- ============================================================================
-- User Sessions Table
-- ============================================================================
-- Manages web application sessions
CREATE TABLE user_sessions (
    session_id VARCHAR(255) PRIMARY KEY COMMENT 'Unique session identifier',
    user_id INT NOT NULL COMMENT 'User associated with this session',
    session_data TEXT COMMENT 'Serialized session data',
    ip_address VARCHAR(45) COMMENT 'IP address of the session',
    user_agent TEXT COMMENT 'Browser/client user agent',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL COMMENT 'Session expiration time',
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether session is active',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
) COMMENT='Web application session management';

-- ============================================================================
-- Views for Common Queries
-- ============================================================================

-- User storage statistics view
CREATE OR REPLACE VIEW user_storage_stats AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.storage_quota,
    u.storage_used,
    ROUND((u.storage_used / u.storage_quota) * 100, 2) AS usage_percentage,
    (u.storage_quota - u.storage_used) AS available_storage,
    COUNT(f.id) AS file_count,
    u.created_at AS account_created
FROM users u
LEFT JOIN files f ON u.id = f.user_id
GROUP BY u.id, u.username, u.email, u.storage_quota, u.storage_used, u.created_at;

-- Recent activity view
CREATE OR REPLACE VIEW recent_activity AS
SELECT 
    al.id,
    al.user_id,
    u.username,
    al.action,
    al.resource_type,
    al.resource_id,
    al.details,
    al.created_at
FROM activity_logs al
JOIN users u ON al.user_id = u.id
ORDER BY al.created_at DESC
LIMIT 100;

-- Shared files view
CREATE OR REPLACE VIEW shared_files_view AS
SELECT 
    fs.id AS share_id,
    fs.file_id,
    f.filename,
    f.file_size,
    f.mime_type,
    fs.shared_by_user_id,
    u1.username AS shared_by_username,
    fs.shared_with_user_id,
    u2.username AS shared_with_username,
    fs.share_token,
    fs.is_public,
    fs.expires_at,
    fs.created_at AS shared_at
FROM file_shares fs
JOIN files f ON fs.file_id = f.id
JOIN users u1 ON fs.shared_by_user_id = u1.id
LEFT JOIN users u2 ON fs.shared_with_user_id = u2.id;

-- ============================================================================
-- Stored Procedures
-- ============================================================================

DELIMITER //

-- Update user storage usage
CREATE PROCEDURE update_user_storage(
    IN p_user_id INT,
    IN p_file_size BIGINT
)
BEGIN
    UPDATE users 
    SET storage_used = storage_used + p_file_size
    WHERE id = p_user_id;
END //

-- Check if user has enough storage quota
CREATE PROCEDURE check_storage_quota(
    IN p_user_id INT,
    IN p_file_size BIGINT,
    OUT p_has_space BOOLEAN
)
BEGIN
    DECLARE current_used BIGINT;
    DECLARE quota BIGINT;
    
    SELECT storage_used, storage_quota 
    INTO current_used, quota
    FROM users 
    WHERE id = p_user_id;
    
    SET p_has_space = (current_used + p_file_size) <= quota;
END //

-- Log user activity
CREATE PROCEDURE log_activity(
    IN p_user_id INT,
    IN p_action VARCHAR(50),
    IN p_resource_type VARCHAR(50),
    IN p_resource_id INT,
    IN p_details TEXT
)
BEGIN
    INSERT INTO activity_logs (user_id, action, resource_type, resource_id, details)
    VALUES (p_user_id, p_action, p_resource_type, p_resource_id, p_details);
END //

-- Clean up expired sessions
CREATE PROCEDURE cleanup_expired_sessions()
BEGIN
    DELETE FROM user_sessions 
    WHERE expires_at < NOW() OR is_active = FALSE;
END //

-- Clean up expired shares
CREATE PROCEDURE cleanup_expired_shares()
BEGIN
    DELETE FROM file_shares 
    WHERE expires_at IS NOT NULL AND expires_at < NOW();
END //

DELIMITER ;

-- ============================================================================
-- Initial Data (Optional - Commented Out)
-- ============================================================================
-- No default users - users must sign up through the application
-- This ensures security and proper password hashing

-- Example: Insert a test user (password: 'password123' hashed with SHA-256)
-- INSERT INTO users (username, email, password, storage_quota, storage_used) 
-- VALUES ('testuser', 'test@example.com', 'EF92B778BAFE771E89245B89ECBC08A44A4E166C06659911881F383D4473E94F', 1073741824, 0);

-- ============================================================================
-- Database Setup Complete
-- ============================================================================

SELECT 'PatraKosh database schema created successfully!' AS Status;
SELECT COUNT(*) AS total_tables 
FROM information_schema.tables 
WHERE table_schema = 'patrakosh_db' AND table_type = 'BASE TABLE';

-- Display table information
SELECT 
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'patrakosh_db' AND table_type = 'BASE TABLE'
ORDER BY table_name;
