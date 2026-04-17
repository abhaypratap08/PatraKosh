# PatraKosh

A secure, self-hosted file storage app — think personal Dropbox — built with Spring Boot and React.

Store, share, and manage files locally with features like expiring share links, activity history, search, and storage stats. Includes both a web client and a JavaFX desktop client.

**Stack:** Java 17 · Spring Boot 3 · React 18 · Vite 5 · JavaFX 21

**Quick start:**
```bash
# API
mvn -Dspring-boot.run.mainClass=com.patrakosh.api.ApiApplication spring-boot:run

# Web client
cd frontend && npm ci && npm run dev
```

See the [full docs](src/main/resources/application.properties) for HTTPS setup, config options, and environment variables.

**Live demo:** https://abhaypratap08.github.io/PatraKosh/

MIT License
