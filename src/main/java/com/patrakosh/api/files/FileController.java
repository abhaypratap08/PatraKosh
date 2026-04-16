package com.patrakosh.api.files;

import com.patrakosh.api.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final AuthService authService;
    private final FileStorageService fileStorageService;

    public FileController(AuthService authService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<FileStorageService.FileView> listFiles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "q", required = false) String query
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        return fileStorageService.listFiles(user.id(), query);
    }

    @GetMapping("/stats")
    public FileStorageService.StorageStats getStats(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        return fileStorageService.getStats(user.id());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileStorageService.FileView> upload(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam("file") MultipartFile file
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        return ResponseEntity.status(201).body(fileStorageService.store(user, file));
    }

    @PutMapping("/{fileId}")
    public FileStorageService.FileView renameFile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("fileId") long fileId,
            @Valid @RequestBody RenameFileRequest request
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        return fileStorageService.renameFile(user.id(), fileId, request.filename());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("fileId") long fileId
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        fileStorageService.deleteFile(user.id(), fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("fileId") long fileId
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        FileStorageService.StoredFile storedFile = fileStorageService.prepareDownload(user.id(), fileId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            mediaType = MediaType.parseMediaType(storedFile.mimeType());
        } catch (IllegalArgumentException ignored) {
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(storedFile.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new FileSystemResource(storedFile.storagePath()));
    }

    public record RenameFileRequest(@NotBlank(message = "Filename is required") String filename) {
    }
}
