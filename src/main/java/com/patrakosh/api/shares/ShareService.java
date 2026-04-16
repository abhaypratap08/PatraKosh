package com.patrakosh.api.shares;

import com.patrakosh.api.activity.ActivityService;
import com.patrakosh.api.auth.AuthService;
import com.patrakosh.api.files.FileStorageService;
import com.patrakosh.persistence.AppStateStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

@Service
public class ShareService {

    private static final int DEFAULT_EXPIRY_HOURS = 24;
    private static final int MAX_EXPIRY_HOURS = 24 * 30;

    private final AppStateStore stateStore;
    private final FileStorageService fileStorageService;
    private final ActivityService activityService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ShareService(
            AppStateStore stateStore,
            FileStorageService fileStorageService,
            ActivityService activityService
    ) {
        this.stateStore = stateStore;
        this.fileStorageService = fileStorageService;
        this.activityService = activityService;
    }

    public ShareView createShare(AuthService.UserAccount user, long fileId, Integer expiresInHours) {
        int ttlHours = expiresInHours == null ? DEFAULT_EXPIRY_HOURS : expiresInHours;
        if (ttlHours < 1 || ttlHours > MAX_EXPIRY_HOURS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Share expiry must be between 1 and 720 hours");
        }

        FileStorageService.StoredFile file = fileStorageService.getOwnedStoredFile(user.id(), fileId);
        ShareView shareView = stateStore.write(state -> {
            Instant now = Instant.now();
            state.shares.removeIf(share -> isInactive(share, now));

            AppStateStore.ShareRecord shareRecord = new AppStateStore.ShareRecord(
                    state.nextShareId++,
                    fileId,
                    user.id(),
                    randomToken(),
                    now,
                    now.plusSeconds(ttlHours * 3600L),
                    null,
                    0
            );
            state.shares.add(shareRecord);
            return toView(shareRecord, file.filename());
        });

        activityService.record(user.id(), "SHARE", file.filename());
        return shareView;
    }

    public List<ShareView> listShares(AuthService.UserAccount user, long fileId) {
        FileStorageService.StoredFile file = fileStorageService.getOwnedStoredFile(user.id(), fileId);
        return stateStore.read(state -> {
            Instant now = Instant.now();
            return state.shares.stream()
                    .filter(share -> share.fileId == fileId && share.ownerUserId == user.id())
                    .filter(share -> !isInactive(share, now))
                    .sorted(Comparator.comparing((AppStateStore.ShareRecord share) -> share.createdAt).reversed())
                    .map(share -> toView(share, file.filename()))
                    .toList();
        });
    }

    public void revokeShare(AuthService.UserAccount user, long fileId, long shareId) {
        String filename = fileStorageService.getOwnedStoredFile(user.id(), fileId).filename();
        stateStore.write(state -> {
            AppStateStore.ShareRecord shareRecord = state.shares.stream()
                    .filter(share -> share.id == shareId && share.fileId == fileId && share.ownerUserId == user.id())
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share not found"));

            if (!isInactive(shareRecord, Instant.now())) {
                shareRecord.revokedAt = Instant.now();
            }
            return null;
        });
        activityService.record(user.id(), "UNSHARE", filename);
    }

    public SharedDownload prepareSharedDownload(String token) {
        SharedDownload sharedDownload = stateStore.write(state -> {
            Instant now = Instant.now();
            AppStateStore.ShareRecord shareRecord = state.shares.stream()
                    .filter(share -> token.equals(share.token))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));

            if (isInactive(shareRecord, now)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link is no longer active");
            }

            shareRecord.accessCount++;
            FileStorageService.StoredFile storedFile = fileStorageService.getStoredFile(shareRecord.fileId);
            if (!java.nio.file.Files.exists(storedFile.storagePath())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared file is no longer available");
            }
            return new SharedDownload(
                    shareRecord.id,
                    shareRecord.ownerUserId,
                    storedFile.filename(),
                    storedFile.mimeType(),
                    storedFile.storagePath(),
                    shareRecord.expiresAt,
                    shareRecord.accessCount
            );
        });

        activityService.record(sharedDownload.ownerUserId(), "SHARED_DOWNLOAD", sharedDownload.filename());
        return sharedDownload;
    }

    private ShareView toView(AppStateStore.ShareRecord shareRecord, String filename) {
        return new ShareView(
                shareRecord.id,
                shareRecord.token,
                filename,
                shareRecord.createdAt,
                shareRecord.expiresAt,
                shareRecord.accessCount
        );
    }

    private boolean isInactive(AppStateStore.ShareRecord shareRecord, Instant now) {
        return shareRecord.revokedAt != null
                || shareRecord.expiresAt == null
                || !shareRecord.expiresAt.isAfter(now);
    }

    private String randomToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    public record ShareView(
            long id,
            String token,
            String filename,
            Instant createdAt,
            Instant expiresAt,
            long accessCount
    ) {
    }

    public record SharedDownload(
            long shareId,
            long ownerUserId,
            String filename,
            String mimeType,
            java.nio.file.Path storagePath,
            Instant expiresAt,
            long accessCount
    ) {
    }
}
