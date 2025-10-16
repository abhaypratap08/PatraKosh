-- ============================================================================
-- PatraKosh Database Migration Script for Rubric Compliance
-- This script enhances the existing schema with new tables and columns
-- ============================================================================

USE patrakosh_db;

-- ============================================================================
-- STEP 1: Enhance existing users table
-- ============================================================================

-- Add storage quota and usage tracking columns
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS storage_quota BIGINT DEFAULT 1073741824 COMMENT '1GB default quota in bytes',
ADD COLUMN IF NOT EXISTS storage_used BIGINT DEFAULT 0 COMMENT 'Current storage usage in bytes',
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ============================================================================
-- STEP 2: Enhance existing files table
-- ============================================================================

-- Add file hash, MIME type, version, and audit columns
ALTER TABLE files
ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64) COMMENT 'SHA-256 hash for duplicate detection',
ADD COLUMN IF NOT EXISTS mime_type VARCHAR(100) COMMENT 'File MIME type',
ADD COLUMN IF NOT EXISTS version INT DEFAULT 1 COMMENT 'File version number',
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Add indexes for improved query performance
CREATE INDEX IF NOT EXISTS idx_file_hash ON files(file_hash);
CREATE INDEX IF NOT EXISTS idx_user_filename ON files(user_id, filename);
CREATE INDEX IF NOT EXISTS idx_mime_type ON files(mime_type);

-- ============================================================================
-- STEP 3: Create file_shares table
-- ============================================================================

CREATE TABLE IF NOT EXISTS file_shares (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL COMMENT 'Reference to shared file',
    shared_by_user_id INT NOT NULL COMMENT 'User who shared the file',
    shared_with_user_id INT NULL COMMENT 'User receiving the share (NULL for public shares)',
    share_token VARCHAR(255) UNIQUE COMMENT 'Unique token for public link access',
    expires_at TIMESTAMP NULL COMMENT 'Share expiration timestamp',
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
) COMMENT='Tracks file sharing between users';

-- ============================================================================
-- STEP 4: Create activity_logs table
-- ============================================================================

CREATE TABLE IF NOT EXISTS activity_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'User who performed the action',
    action VARCHAR(50) NOT NULL COMMENT 'Action type: UPLOAD, DOWNLOAD, DELETE, SHARE, LOGIN, LOGOUT',
    resource_type VARCHAR(50) NOT NULL COMMENT 'Resource type: FILE, USER, SHARE',
    resource_id INT COMMENT 'ID of the affected resource',
    details TEXT COMMENT 'Additional details about the action',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_action (user_id, action),
    INDEX idx_created_at (created_at),
    INDEX idx_resource (resource_type, resource_id)
) COMMENT='Tracks all user activities in the system';

-- ============================================================================
-- STEP 5: Create file_versions table
-- ============================================================================

CREATE TABLE IF NOT EXISTS file_versions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL COMMENT 'Reference to the main file',
    version_number INT NOT NULL COMMENT 'Version number',
    filepath VARCHAR(500) NOT NULL COMMENT 'Path to the version file',
    file_size BIGINT NOT NULL COMMENT 'Size of this version in bytes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_file_version (file_id, version_number),
    INDEX idx_file_id (file_id),
    INDEX idx_created_at (created_at)
) COMMENT='Stores file version history';

-- ============================================================================
-- STEP 6: Create user_sessions table (for web interface)
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(255) PRIMARY KEY COMMENT 'Unique session identifier',
    user_id INT NOT NULL COMMENT 'User associated with this session',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL COMMENT 'Session expiration time',
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) COMMENT='Manages web application sessions';

-- ============================================================================
-- STEP 7: Create folders table (for file organization)
-- ============================================================================

CREATE TABLE IF NOT EXISTS folders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'Owner of the folder',
    parent_folder_id INT NULL COMMENT 'Parent folder for nested structure',
    folder_name VARCHAR(255) NOT NULL COMMENT 'Name of the folder',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_folder_id) REFERENCES folders(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_parent_folder (parent_folder_id),
    UNIQUE KEY unique_user_folder (user_id, parent_folder_id, folder_name)
) COMMENT='Organizes files into folders';

-- Add folder_id to files table
ALTER TABLE files
ADD COLUMN IF NOT EXISTS folder_id INT NULL COMMENT 'Folder containing this file',
ADD CONSTRAINT fk_files_folder FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_folder_id ON files(folder_id);

-- ============================================================================
-- STEP 8: Insert sample data for testing (optional)
-- ============================================================================

-- Sample user (password is 'password123' hashed with SHA-256)
INSERT IGNORE INTO users (username, email, password, storage_quota, storage_used) 
VALUES ('testuser', 'test@example.com', 'EF92B778BAFE771E89245B89ECBC08A44A4E166C06659911881F383D4473E94F', 1073741824, 0);

-- ============================================================================
-- STEP 9: Create views for common queries
-- ============================================================================

-- View for user storage statistics
CREATE OR REPLACE VIEW user_storage_stats AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.storage_quota,
    u.storage_used,
    ROUND((u.storage_used / u.storage_quota) * 100, 2) AS usage_percentage,
    COUNT(f.id) AS file_count
FROM users u
LEFT JOIN files f ON u.id = f.user_id
GROUP BY u.id, u.username, u.email, u.storage_quota, u.storage_used;

-- View for recent activity
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

-- View for shared files
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
    fs.created_at
FROM file_shares fs
JOIN files f ON fs.file_id = f.id
JOIN users u1 ON fs.shared_by_user_id = u1.id
LEFT JOIN users u2 ON fs.shared_with_user_id = u2.id;

-- ============================================================================
-- STEP 10: Create stored procedures for common operations
-- ============================================================================

DELIMITER //

-- Procedure to update user storage usage
CREATE PROCEDURE IF NOT EXISTS update_user_storage(
    IN p_user_id INT,
    IN p_file_size BIGINT
)
BEGIN
    UPDATE users 
    SET storage_used = storage_used + p_file_size
    WHERE id = p_user_id;
END //

-- Procedure to check storage quota
CREATE PROCEDURE IF NOT EXISTS check_storage_quota(
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

-- Procedure to log activity
CREATE PROCEDURE IF NOT EXISTS log_activity(
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

DELIMITER ;

-- ============================================================================
-- Migration Complete
-- ============================================================================

SELECT 'Database migration completed successfully!' AS Status;
SELECT COUNT(*) AS total_tables FROM information_schema.tables WHERE table_schema = 'patrakosh_db';
