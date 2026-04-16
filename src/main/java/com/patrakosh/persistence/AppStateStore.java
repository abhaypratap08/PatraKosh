package com.patrakosh.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class AppStateStore {

    private final ObjectMapper objectMapper;
    private final Path dataRoot;
    private final Path stateFile;

    @Autowired
    public AppStateStore(@Value("${patrakosh.data.base-path:data}") String basePath) {
        this(Path.of(basePath));
    }

    public AppStateStore(Path basePath) {
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.dataRoot = basePath.toAbsolutePath().normalize();
        this.stateFile = dataRoot.resolve("state.json");
        ensureInitialized();
    }

    public synchronized <T> T read(Function<StateSnapshot, T> reader) {
        return reader.apply(loadState());
    }

    public synchronized <T> T write(Function<StateSnapshot, T> writer) {
        StateSnapshot state = loadState();
        T result = writer.apply(state);
        saveState(state);
        return result;
    }

    public synchronized void reset() {
        saveState(new StateSnapshot());
    }

    public Path getDataRoot() {
        return dataRoot;
    }

    private void ensureInitialized() {
        try {
            Files.createDirectories(dataRoot);
            if (!Files.exists(stateFile)) {
                saveState(new StateSnapshot());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize PatraKosh data store", exception);
        }
    }

    private StateSnapshot loadState() {
        try {
            if (!Files.exists(stateFile)) {
                return new StateSnapshot();
            }
            return objectMapper.readValue(stateFile.toFile(), StateSnapshot.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load PatraKosh state", exception);
        }
    }

    private void saveState(StateSnapshot state) {
        try {
            Files.createDirectories(dataRoot);
            Path tempFile = dataRoot.resolve("state.json.tmp");
            objectMapper.writeValue(tempFile.toFile(), state);
            try {
                Files.move(tempFile, stateFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, stateFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save PatraKosh state", exception);
        }
    }

    public static final class StateSnapshot {
        public long nextUserId = 1;
        public long nextFileId = 1;
        public long nextActivityId = 1;
        public long nextShareId = 1;
        public List<UserRecord> users = new ArrayList<>();
        public List<SessionRecord> sessions = new ArrayList<>();
        public List<FileRecord> files = new ArrayList<>();
        public List<ShareRecord> shares = new ArrayList<>();
        public List<ActivityRecord> activities = new ArrayList<>();
    }

    public static final class UserRecord {
        public long id;
        public String username;
        public String email;
        public String passwordHash;
        public Instant createdAt;

        public UserRecord() {
        }

        public UserRecord(long id, String username, String email, String passwordHash, Instant createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
        }
    }

    public static final class SessionRecord {
        public String token;
        public long userId;
        public Instant createdAt;
        public Instant expiresAt;
        public Instant revokedAt;

        public SessionRecord() {
        }

        public SessionRecord(String token, long userId, Instant createdAt, Instant expiresAt, Instant revokedAt) {
            this.token = token;
            this.userId = userId;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.revokedAt = revokedAt;
        }
    }

    public static final class FileRecord {
        public long id;
        public long userId;
        public String filename;
        public String mimeType;
        public long fileSize;
        public Instant uploadTime;
        public String storageKey;

        public FileRecord() {
        }

        public FileRecord(long id, long userId, String filename, String mimeType, long fileSize, Instant uploadTime, String storageKey) {
            this.id = id;
            this.userId = userId;
            this.filename = filename;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
            this.uploadTime = uploadTime;
            this.storageKey = storageKey;
        }
    }

    public static final class ShareRecord {
        public long id;
        public long fileId;
        public long ownerUserId;
        public String token;
        public Instant createdAt;
        public Instant expiresAt;
        public Instant revokedAt;
        public long accessCount;

        public ShareRecord() {
        }

        public ShareRecord(
                long id,
                long fileId,
                long ownerUserId,
                String token,
                Instant createdAt,
                Instant expiresAt,
                Instant revokedAt,
                long accessCount
        ) {
            this.id = id;
            this.fileId = fileId;
            this.ownerUserId = ownerUserId;
            this.token = token;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.revokedAt = revokedAt;
            this.accessCount = accessCount;
        }
    }

    public static final class ActivityRecord {
        public long id;
        public long userId;
        public String action;
        public String filename;
        public Instant createdAt;

        public ActivityRecord() {
        }

        public ActivityRecord(long id, long userId, String action, String filename, Instant createdAt) {
            this.id = id;
            this.userId = userId;
            this.action = action;
            this.filename = filename;
            this.createdAt = createdAt;
        }
    }
}
