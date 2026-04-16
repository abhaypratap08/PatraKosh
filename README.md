## Description
PatraKosh is a secure, Dropbox-like file storage application with:

- a Spring Boot API for signup, login, logout, upload, rename, delete, download, expiring share links, stats, and activity history
- a React frontend for the web flow
- a JavaFX desktop demo that reuses the persisted account store for login/signup and keeps local desktop files scoped per user

The current backend stores uploaded files and app state on the local filesystem by default, uses HttpOnly session cookies for the web session, and rate-limits auth and public share downloads. It runs without MySQL, MinIO, or S3.

## Tech Stack
React, Vite, Java 17, Spring Boot, JavaFX, REST API, local filesystem storage

## Local Development

### Run the API
```bash
mvn -Dspring-boot.run.mainClass=com.patrakosh.api.ApiApplication spring-boot:run
```

### Run the API over HTTPS
```bash
./scripts/ensure-dev-https-cert.sh
mvn -Dspring-boot.run.mainClass=com.patrakosh.api.ApiApplication -Dspring-boot.run.profiles=https spring-boot:run
```

### Run the frontend
```bash
cd frontend
npm ci
npm run dev
```

If `.certs/patrakosh-dev.p12` exists, the Vite dev server will also start over `https://127.0.0.1:5173` and proxy `/api` to the HTTPS API on `https://127.0.0.1:8443`.

### Run the desktop app
```bash
mvn javafx:run
```

### Storage location
The API writes uploads to `storage/` by default and persists users, sessions, shares, and activity to `data/state.json`.

To override it:
```bash
export PATRAKOSH_STORAGE_BASE_PATH=/absolute/path/to/storage
export PATRAKOSH_DATA_BASE_PATH=/absolute/path/to/data
```

## Verification
```bash
mvn clean test
cd frontend && npm run build
```

## Live Test Link
https://abhaypratap08.github.io/PatraKosh/

## License
MIT License

Copyright (c) 2026 TEAM ALGONAUTS

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

{TEAM ALGONAUTS}
