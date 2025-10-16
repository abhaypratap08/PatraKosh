package com.patrakosh.service;

import com.patrakosh.dao.ActivityLogDAO;
import com.patrakosh.dao.FileShareDAO;
import com.patrakosh.exception.DatabaseException;
import com.patrakosh.exception.ValidationException;
import com.patrakosh.model.FileShare;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShareService extends BaseService {
    private final FileShareDAO fileShareDAO;
    private final ActivityLogDAO activityLogDAO;
    
    public ShareService() {
        super();
        this.fileShareDAO = new FileShareDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }
    
    public FileShare createShare(Integer fileId, Integer sharedByUserId, Integer sharedWithUserId, boolean isPublic, LocalDateTime expiresAt) throws DatabaseException {
        logOperation("createShare", fileId, sharedByUserId, sharedWithUserId);
        
        return transactionManager.executeInTransaction(conn -> {
            FileShare share = new FileShare();
            share.setFileId(fileId);
            share.setSharedByUserId(sharedByUserId);
            share.setSharedWithUserId(sharedWithUserId);
            share.setPublic(isPublic);
            share.setExpiresAt(expiresAt);
            
            if (isPublic) {
                share.setShareToken(UUID.randomUUID().toString());
            }
            
            FileShare savedShare = fileShareDAO.save(share);
            activityLogDAO.logActivity(sharedByUserId, "SHARE", "FILE", fileId, "Shared file");
            
            logger.info("File share created: {}", savedShare.getId());
            return savedShare;
        });
    }
    
    public List<FileShare> getSharesForUser(Integer userId) throws DatabaseException {
        return fileShareDAO.getSharesForUser(userId);
    }
    
    public FileShare getShareByToken(String token) throws DatabaseException {
        return fileShareDAO.getShareByToken(token);
    }
    
    public boolean revokeShare(Integer shareId, Integer userId) throws DatabaseException {
        return transactionManager.executeInTransaction(conn -> {
            boolean deleted = fileShareDAO.delete(shareId);
            if (deleted) {
                activityLogDAO.logActivity(userId, "REVOKE_SHARE", "SHARE", shareId);
            }
            return deleted;
        });
    }
    
    @Override
    protected void validateInput(Object input) throws ValidationException {
        if (input == null) {
            throw new ValidationException("Input cannot be null");
        }
    }
}
