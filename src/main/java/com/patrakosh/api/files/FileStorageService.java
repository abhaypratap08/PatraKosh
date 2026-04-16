package com.patrakosh.api.files;

import com.patrakosh.api.activity.ActivityService;
import com.patrakosh.api.auth.AuthService;
import com.patrakosh.persistence.AppStateStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private final AppStateStore stateStore;
    private final ActivityService activityService;
    private final Path storageRoot;

    public FileStorageService(
            AppStateStore stateStore,
            ActivityService activityService,
            @Value("${patrakosh.storage.base-path:storage}") String basePath
    ) {
        this.stateStore = stateStore;
        this.activityService = activityService;
        this.storageRoot = Path.of(basePath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize storage directory", exception);
        }
    }

    public FileView store(AuthService.UserAccount user, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a file to upload");
        }

        String originalFilename = sanitizeFilename(multipartFile.getOriginalFilename());
        if (originalFilename.isBlank()) {
            originalFilename = "upload.bin";
        }
        String storedFilename = originalFilename;

        String storageKey = "user-" + user.id() + "/" + UUID.randomUUID() + extensionOf(storedFilename);
        Path target = resolveStoragePath(storageKey);

        try {
            Files.createDirectories(target.getParent());
            try (var inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store the uploaded file");
        }

        try {
            FileView fileView = stateStore.write(state -> {
                AppStateStore.FileRecord fileRecord = new AppStateStore.FileRecord(
                        state.nextFileId++,
                        user.id(),
                        storedFilename,
                        contentTypeOf(multipartFile),
                        multipartFile.getSize(),
                        Instant.now(),
                        storageKey
                );
                state.files.add(fileRecord);
                return toView(fileRecord);
            });
            activityService.record(user.id(), "UPLOAD", storedFilename);
            return fileView;
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
            throw exception;
        }
    }

    public List<FileView> listFiles(long userId, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return stateStore.read(state -> state.files.stream()
                .filter(file -> file.userId == userId)
                .filter(file -> normalizedQuery.isBlank() || file.filename.toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .sorted(Comparator.comparing((AppStateStore.FileRecord file) -> file.uploadTime).reversed())
                .map(this::toView)
                .toList());
    }

    public StorageStats getStats(long userId) {
        return stateStore.read(state -> {
            long fileCount = state.files.stream()
                    .filter(file -> file.userId == userId)
                    .count();
            long storageUsed = state.files.stream()
                    .filter(file -> file.userId == userId)
                    .mapToLong(file -> file.fileSize)
                    .sum();
            return new StorageStats(fileCount, storageUsed);
        });
    }

    public FileView renameFile(long userId, long fileId, String filename) {
        String nextFilename = sanitizeFilename(filename);
        if (nextFilename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename cannot be empty");
        }

        FileView fileView = stateStore.write(state -> {
            AppStateStore.FileRecord fileRecord = findOwnedFile(state, userId, fileId);
            fileRecord.filename = nextFilename;
            return toView(fileRecord);
        });
        activityService.record(userId, "RENAME", nextFilename);
        return fileView;
    }

    public void deleteFile(long userId, long fileId) {
        StoredFile storedFile = stateStore.write(state -> {
            AppStateStore.FileRecord fileRecord = findOwnedFile(state, userId, fileId);
            state.files.remove(fileRecord);
            state.shares.removeIf(share -> share.fileId == fileId);
            return toStoredFile(fileRecord);
        });

        try {
            Files.deleteIfExists(storedFile.storagePath());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the file");
        }

        activityService.record(userId, "DELETE", storedFile.filename());
    }

    public StoredFile prepareDownload(long userId, long fileId) {
        StoredFile storedFile = stateStore.write(state -> toStoredFile(findOwnedFile(state, userId, fileId)));
        if (!Files.exists(storedFile.storagePath())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stored file is missing");
        }
        activityService.record(userId, "DOWNLOAD", storedFile.filename());
        return storedFile;
    }

    public StoredFile getOwnedStoredFile(long userId, long fileId) {
        return stateStore.read(state -> toStoredFile(findOwnedFile(state, userId, fileId)));
    }

    public StoredFile getStoredFile(long fileId) {
        return stateStore.read(state -> state.files.stream()
                .filter(file -> file.id == fileId)
                .findFirst()
                .map(this::toStoredFile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")));
    }

    private AppStateStore.FileRecord findOwnedFile(AppStateStore.StateSnapshot state, long userId, long fileId) {
        return state.files.stream()
                .filter(file -> file.id == fileId && file.userId == userId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }

    private FileView toView(AppStateStore.FileRecord fileRecord) {
        return new FileView(
                fileRecord.id,
                fileRecord.filename,
                fileRecord.fileSize,
                fileRecord.mimeType,
                fileRecord.uploadTime
        );
    }

    private StoredFile toStoredFile(AppStateStore.FileRecord fileRecord) {
        return new StoredFile(
                fileRecord.id,
                fileRecord.userId,
                fileRecord.filename,
                fileRecord.mimeType,
                fileRecord.fileSize,
                fileRecord.uploadTime,
                resolveStoragePath(fileRecord.storageKey)
        );
    }

    private Path resolveStoragePath(String storageKey) {
        Path resolved = storageRoot.resolve(storageKey).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid storage path");
        }
        return resolved;
    }

    private static String sanitizeFilename(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        String filename = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        return filename.trim().replaceAll("[\\r\\n]+", "_");
    }

    private static String contentTypeOf(MultipartFile multipartFile) {
        return multipartFile.getContentType() == null || multipartFile.getContentType().isBlank()
                ? "application/octet-stream"
                : multipartFile.getContentType();
    }

    private static String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }

    public record FileView(long id, String filename, long fileSize, String mimeType, Instant uploadTime) {
    }

    public record StorageStats(long fileCount, long storageUsed) {
    }

    public record StoredFile(
            long id,
            long userId,
            String filename,
            String mimeType,
            long fileSize,
            Instant uploadTime,
            Path storagePath
    ) {
    }
}
