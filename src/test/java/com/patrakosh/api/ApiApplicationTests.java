package com.patrakosh.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrakosh.api.config.RequestRateLimiter;
import com.patrakosh.persistence.AppStateStore;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "patrakosh.storage.base-path=target/test-storage",
        "patrakosh.data.base-path=target/test-data",
        "patrakosh.auth.session-ttl-seconds=3600",
        "patrakosh.auth.cookie-name=PATRAKOSH_SESSION",
        "patrakosh.auth.rate-limit.login.max-attempts=3",
        "patrakosh.auth.rate-limit.login.window-seconds=60",
        "patrakosh.auth.rate-limit.signup.max-attempts=3",
        "patrakosh.auth.rate-limit.signup.window-seconds=60",
        "patrakosh.shares.rate-limit.download.max-attempts=3",
        "patrakosh.shares.rate-limit.download.window-seconds=60",
        "patrakosh.cors.allowed-origins=http://localhost:5173"
})
class ApiApplicationTests {

    private static final Path TEST_STORAGE = Path.of("target/test-storage");
    private static final Path TEST_DATA = Path.of("target/test-data");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppStateStore appStateStore;

    @Autowired
    private RequestRateLimiter requestRateLimiter;

    @BeforeEach
    void resetState() throws IOException {
        cleanDirectory(TEST_STORAGE);
        cleanDirectory(TEST_DATA);
        appStateStore.reset();
        requestRateLimiter.reset();
    }

    @AfterEach
    void cleanState() throws IOException {
        cleanDirectory(TEST_STORAGE);
        cleanDirectory(TEST_DATA);
    }

    @Test
    void signupUploadRenameShareDownloadAndDeleteFlowWorks() throws Exception {
        SessionFixture session = signup("demo", "demo@example.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello world".getBytes(StandardCharsets.UTF_8)
        );

        String uploadBody = mockMvc.perform(
                        multipart("/api/files")
                                .file(file)
                                .cookie(session.cookie())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("notes.txt"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode uploadJson = objectMapper.readTree(uploadBody);
        long fileId = uploadJson.get("id").asLong();

        mockMvc.perform(
                        put("/api/files/{fileId}", fileId)
                                .cookie(session.cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"filename":"renamed.txt"}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("renamed.txt"));

        String shareBody = mockMvc.perform(
                        post("/api/files/{fileId}/shares", fileId)
                                .cookie(session.cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "expiresInHours": 24
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("renamed.txt"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode shareJson = objectMapper.readTree(shareBody);
        URI shareUri = URI.create(shareJson.get("shareUrl").asText());

        mockMvc.perform(get(shareUri.getPath()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("renamed.txt")))
                .andExpect(content().bytes("hello world".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/files").cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("renamed.txt"));

        mockMvc.perform(get("/api/files/stats").cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileCount").value(1))
                .andExpect(jsonPath("$.storageUsed").value(11));

        mockMvc.perform(get("/api/activity").cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", hasItems("SIGNUP", "UPLOAD", "RENAME", "SHARE", "SHARED_DOWNLOAD")));

        AppStateStore reloadedStore = new AppStateStore(TEST_DATA);
        int persistedUsers = reloadedStore.read(state -> state.users.size());
        int persistedSessions = reloadedStore.read(state -> state.sessions.size());
        int persistedFiles = reloadedStore.read(state -> state.files.size());
        int persistedShares = reloadedStore.read(state -> state.shares.size());
        assertThat(persistedUsers).isEqualTo(1);
        assertThat(persistedSessions).isEqualTo(1);
        assertThat(persistedFiles).isEqualTo(1);
        assertThat(persistedShares).isEqualTo(1);

        mockMvc.perform(delete("/api/files/{fileId}", fileId).cookie(session.cookie()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/files/stats").cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileCount").value(0))
                .andExpect(jsonPath("$.storageUsed").value(0));
    }

    @Test
    void logoutRevokesTheCurrentToken() throws Exception {
        SessionFixture session = signup("demo", "demo@example.com");

        mockMvc.perform(post("/api/auth/logout").cookie(session.cookie()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").cookie(session.cookie()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersCannotAccessEachOthersFiles() throws Exception {
        SessionFixture ownerSession = signup("owner", "owner@example.com");
        SessionFixture guestSession = signup("guest", "guest@example.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "owner.txt",
                "text/plain",
                "secret".getBytes(StandardCharsets.UTF_8)
        );

        String uploadBody = mockMvc.perform(
                        multipart("/api/files")
                                .file(file)
                                .cookie(ownerSession.cookie())
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long fileId = objectMapper.readTree(uploadBody).get("id").asLong();

        mockMvc.perform(get("/api/files/{fileId}/download", fileId).cookie(guestSession.cookie()))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        put("/api/files/{fileId}", fileId)
                                .cookie(guestSession.cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"filename":"stolen.txt"}
                                        """)
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/files/{fileId}", fileId).cookie(guestSession.cookie()))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/api/files/{fileId}/shares", fileId)
                                .cookie(guestSession.cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shareLinksCanBeListedAndRevoked() throws Exception {
        SessionFixture session = signup("demo", "demo@example.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "shared.txt",
                "text/plain",
                "shared".getBytes(StandardCharsets.UTF_8)
        );

        String uploadBody = mockMvc.perform(
                        multipart("/api/files")
                                .file(file)
                                .cookie(session.cookie())
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long fileId = objectMapper.readTree(uploadBody).get("id").asLong();

        String shareBody = mockMvc.perform(
                        post("/api/files/{fileId}/shares", fileId)
                                .cookie(session.cookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "expiresInHours": 12
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode shareJson = objectMapper.readTree(shareBody);
        long shareId = shareJson.get("id").asLong();
        URI shareUri = URI.create(shareJson.get("shareUrl").asText());

        mockMvc.perform(get("/api/files/{fileId}/shares", fileId).cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(shareId));

        mockMvc.perform(delete("/api/files/{fileId}/shares/{shareId}", fileId, shareId)
                        .cookie(session.cookie()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(shareUri.getPath()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/files/{fileId}/shares", fileId).cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void duplicateSignupReturnsFieldErrors() throws Exception {
        signup("demo", "demo@example.com");

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "demo",
                                          "email": "another@example.com",
                                          "password": "password123",
                                          "confirmPassword": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").value("This username is already taken."));
    }

    @Test
    void loginIsRateLimitedAndAuthCookieIsHttpOnly() throws Exception {
        SessionFixture session = signup("demo", "demo@example.com");

        mockMvc.perform(get("/api/auth/me").cookie(session.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo"));

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(
                            post("/api/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "usernameOrEmail": "demo",
                                              "password": "wrong-password"
                                            }
                                            """)
                    )
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "usernameOrEmail": "demo",
                                          "password": "wrong-password"
                                        }
                                        """)
                )
                .andExpect(status().isTooManyRequests());
    }

    private static void cleanDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (var stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }
    }

    private SessionFixture signup(String username, String email) throws Exception {
        var result = mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "email": "%s",
                                          "password": "password123",
                                          "confirmPassword": "password123"
                                        }
                                        """.formatted(username, email))
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("PATRAKOSH_SESSION="),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("SameSite=Strict")
                )))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        return new SessionFixture(sessionCookie(setCookie));
    }

    private static Cookie sessionCookie(String setCookieHeader) {
        String firstSegment = setCookieHeader.split(";", 2)[0];
        int separatorIndex = firstSegment.indexOf('=');
        return new Cookie(firstSegment.substring(0, separatorIndex), firstSegment.substring(separatorIndex + 1));
    }

    private record SessionFixture(Cookie cookie) {
    }
}
