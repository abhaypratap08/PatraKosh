package com.patrakosh.util;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FileUtil {
    
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    public static String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";
        
        long weeks = days / 7;
        if (weeks < 4) return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) return months + " month" + (months > 1 ? "s" : "") + " ago";
        
        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }
    
    public static String getFileIcon(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        switch (extension) {
            case "pdf":
                return "📄";
            case "doc":
            case "docx":
                return "📝";
            case "xls":
            case "xlsx":
                return "📊";
            case "ppt":
            case "pptx":
                return "📽️";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "🖼️";
            case "mp4":
            case "avi":
            case "mkv":
                return "🎬";
            case "mp3":
            case "wav":
            case "flac":
                return "🎵";
            case "zip":
            case "rar":
            case "7z":
                return "🗜️";
            case "txt":
                return "📃";
            default:
                return "📁";
        }
    }
    
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
