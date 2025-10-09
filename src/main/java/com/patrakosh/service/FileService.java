package com.patrakosh.service;

import com.patrakosh.dao.FileDAO;
import com.patrakosh.model.FileItem;
import com.patrakosh.util.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class FileService {
    private final FileDAO fileDAO;
    
    public FileService() {
        this.fileDAO = new FileDAO();
    }
    
    public FileItem uploadFile(int userId, File sourceFile) throws SQLException, IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("File does not exist");
        }
        
        // Create user directory if it doesn't exist
        String userStoragePath = Config.getStorageBasePath() + "/user_" + userId;
        File userDir = new File(userStoragePath);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        
        String filename = sourceFile.getName();
        String destinationPath = userStoragePath + "/" + filename;
        File destFile = new File(destinationPath);
        
        int counter = 1;
        while (destFile.exists()) {
            String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            String extension = filename.substring(filename.lastIndexOf('.'));
            filename = nameWithoutExt + "_" + counter + extension;
            destinationPath = userStoragePath + "/" + filename;
            destFile = new File(destinationPath);
            counter++;
        }
        
        // Copy file to storage
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // Save file metadata to database
        FileItem fileItem = new FileItem(userId, filename, destinationPath, sourceFile.length());
        return fileDAO.saveFile(fileItem);
    }
    
    public List<FileItem> getUserFiles(int userId) throws SQLException {
        return fileDAO.getFilesByUserId(userId);
    }
    
    public boolean downloadFile(FileItem fileItem, File destinationFile) throws IOException {
        File sourceFile = new File(fileItem.getFilepath());
        
        if (!sourceFile.exists()) {
            throw new IOException("Source file not found");
        }
        
        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }
    
    public boolean deleteFile(int fileId) throws SQLException, IOException {
        FileItem fileItem = fileDAO.getFileById(fileId);
        
        if (fileItem == null) {
            throw new SQLException("File not found in database");
        }
        
        // Delete physical file
        File file = new File(fileItem.getFilepath());
        if (file.exists()) {
            file.delete();
        }
        
        return fileDAO.deleteFile(fileId);//Deletes from the DB
    }
    
    public List<FileItem> searchFiles(int userId, String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getUserFiles(userId);
        }
        return fileDAO.searchFiles(userId, searchTerm);
    }
    
    public long getTotalStorageUsed(int userId) throws SQLException {
        return fileDAO.getTotalStorageUsed(userId);
    }
    
    public int getFileCount(int userId) throws SQLException {
        return fileDAO.getFileCount(userId);
    }
}
