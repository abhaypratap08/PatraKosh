package com.patrakosh.api.shares;

import com.patrakosh.api.auth.AuthService;
import com.patrakosh.api.config.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@RestController
public class ShareController {

    private final AuthService authService;
    private final ShareService shareService;
    private final RequestRateLimiter requestRateLimiter;
    private final int downloadMaxAttempts;
    private final long downloadWindowSeconds;

    public ShareController(
            AuthService authService,
            ShareService shareService,
            RequestRateLimiter requestRateLimiter,
            @org.springframework.beans.factory.annotation.Value("${patrakosh.shares.rate-limit.download.max-attempts:30}") int downloadMaxAttempts,
            @org.springframework.beans.factory.annotation.Value("${patrakosh.shares.rate-limit.download.window-seconds:60}") long downloadWindowSeconds
    ) {
        this.authService = authService;
        this.shareService = shareService;
        this.requestRateLimiter = requestRateLimiter;
        this.downloadMaxAttempts = downloadMaxAttempts;
        this.downloadWindowSeconds = downloadWindowSeconds;
    }

    @PostMapping("/api/files/{fileId}/shares")
    public ResponseEntity<ShareResponse> createShare(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("fileId") long fileId,
            @Valid @RequestBody(required = false) CreateShareRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        ShareService.ShareView share = shareService.createShare(user, fileId, request == null ? null : request.expiresInHours());
        return ResponseEntity.status(201).body(toResponse(share, servletRequest));
    }

    @GetMapping("/api/files/{fileId}/shares")
    public List<ShareResponse> listShares(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("fileId") long fileId,
            HttpServletRequest servletRequest
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        return shareService.listShares(user, fileId).stream()
                .map(share -> toResponse(share, servletRequest))
                .toList();
    }

    @DeleteMapping("/api/files/{fileId}/shares/{shareId}")
    public ResponseEntity<Void> revokeShare(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("fileId") long fileId,
            @PathVariable("shareId") long shareId
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        shareService.revokeShare(user, fileId, shareId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/shared/{token}/download")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable("token") String token, HttpServletRequest servletRequest) {
        requestRateLimiter.check(
                "share-download:" + clientIp(servletRequest) + ":" + token,
                downloadMaxAttempts,
                Duration.ofSeconds(downloadWindowSeconds),
                "Too many download attempts for this share link. Try again later."
        );

        ShareService.SharedDownload sharedDownload = shareService.prepareSharedDownload(token);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            mediaType = MediaType.parseMediaType(sharedDownload.mimeType());
        } catch (IllegalArgumentException ignored) {
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(sharedDownload.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new FileSystemResource(sharedDownload.storagePath()));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int commaIndex = forwardedFor.indexOf(',');
            return (commaIndex >= 0 ? forwardedFor.substring(0, commaIndex) : forwardedFor).trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private ShareResponse toResponse(ShareService.ShareView share, HttpServletRequest servletRequest) {
        String shareUrl = ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                .replacePath("/api/shared/{token}/download")
                .replaceQuery(null)
                .buildAndExpand(share.token())
                .toUriString();

        return new ShareResponse(
                share.id(),
                share.filename(),
                shareUrl,
                share.createdAt(),
                share.expiresAt(),
                share.accessCount()
        );
    }

    public record CreateShareRequest(
            @Min(value = 1, message = "Expiry must be at least 1 hour")
            @Max(value = 720, message = "Expiry cannot exceed 720 hours")
            Integer expiresInHours
    ) {
    }

    public record ShareResponse(
            long id,
            String filename,
            String shareUrl,
            java.time.Instant createdAt,
            java.time.Instant expiresAt,
            long accessCount
    ) {
    }
}
