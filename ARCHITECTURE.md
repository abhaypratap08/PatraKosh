# PatraKosh - Architecture Documentation

## 🏛️ System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     USER INTERFACE                          │
│                      (JavaFX)                               │
│  ┌──────────┐  ┌──────────┐  ┌────────────────────┐       │
│  │  Login   │  │  Signup  │  │     Dashboard      │       │
│  │  Screen  │  │  Screen  │  │  (File Management) │       │
│  └──────────┘  └──────────┘  └────────────────────┘       │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ FXML Binding
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                   CONTROLLER LAYER                          │
│                   (UI Logic)                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │    Login     │  │    Signup    │  │  Dashboard   │     │
│  │  Controller  │  │  Controller  │  │  Controller  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ Method Calls
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                   SERVICE LAYER                             │
│                   (Business Logic)                          │
│  ┌─────────────────────────┐  ┌────────────────────────┐   │
│  │    AuthService          │  │    FileService         │   │
│  │  - login()              │  │  - uploadFile()        │   │
│  │  - signup()             │  │  - downloadFile()      │   │
│  │  - hashPassword()       │  │  - deleteFile()        │   │
│  │  - validateEmail()      │  │  - searchFiles()       │   │
│  └─────────────────────────┘  └────────────────────────┘   │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ DAO Calls
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                     DAO LAYER                               │
│                  (Data Access)                              │
│  ┌─────────────────────────┐  ┌────────────────────────┐   │
│  │      UserDAO            │  │      FileDAO           │   │
│  │  - createUser()         │  │  - saveFile()          │   │
│  │  - getUserByUsername()  │  │  - getFilesByUserId()  │   │
│  │  - getUserByEmail()     │  │  - deleteFile()        │   │
│  │  - isUsernameExists()   │  │  - searchFiles()       │   │
│  └─────────────────────────┘  └────────────────────────┘   │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ JDBC
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                   DATABASE LAYER                            │
│                     (MySQL)                                 │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │  users table    │         │  files table    │           │
│  │  - id           │         │  - id           │           │
│  │  - username     │         │  - user_id (FK) │           │
│  │  - email        │         │  - filename     │           │
│  │  - password     │         │  - filepath     │           │
│  │  - created_at   │         │  - file_size    │           │
│  └─────────────────┘         │  - upload_time  │           │
│                              └─────────────────┘           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  FILE SYSTEM STORAGE                        │
│                                                             │
│  storage/                                                   │
│    ├── user_1/                                              │
│    │   ├── document.pdf                                     │
│    │   └── image.png                                        │
│    ├── user_2/                                              │
│    │   └── video.mp4                                        │
│    └── user_3/                                              │
│        └── archive.zip                                      │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 Request Flow Diagrams

### 1. User Login Flow

```
┌──────┐
│ User │
└───┬──┘
    │
    │ 1. Enter credentials
    │
┌───▼────────────────┐
│  Login Screen      │
│  (login.fxml)      │
└───┬────────────────┘
    │
    │ 2. Click Login
    │
┌───▼────────────────┐
│ LoginController    │
│ handleLogin()      │
└───┬────────────────┘
    │
    │ 3. Validate input
    │
┌───▼────────────────┐
│  AuthService       │
│  login()           │
└───┬────────────────┘
    │
    │ 4. Hash password
    │ 5. Query database
    │
┌───▼────────────────┐
│    UserDAO         │
│ getUserByUsername()│
└───┬────────────────┘
    │
    │ 6. Execute SQL
    │
┌───▼────────────────┐
│  MySQL Database    │
│  users table       │
└───┬────────────────┘
    │
    │ 7. Return User object
    │
┌───▼────────────────┐
│  AuthService       │
│ Compare passwords  │
└───┬────────────────┘
    │
    │ 8. Return User
    │
┌───▼────────────────┐
│ LoginController    │
│ Set current user   │
└───┬────────────────┘
    │
    │ 9. Navigate to Dashboard
    │
┌───▼────────────────┐
│  Dashboard Screen  │
│  (dashboard.fxml)  │
└────────────────────┘
```

### 2. File Upload Flow

```
┌──────┐
│ User │
└───┬──┘
    │
    │ 1. Click "Upload File"
    │
┌───▼────────────────┐
│ DashboardController│
│ handleUpload()     │
└───┬────────────────┘
    │
    │ 2. Open FileChooser
    │
┌───▼────────────────┐
│  FileChooser       │
│  (JavaFX Dialog)   │
└───┬────────────────┘
    │
    │ 3. User selects file
    │
┌───▼────────────────┐
│ DashboardController│
│ Disable button     │
│ Show "Uploading"   │
└───┬────────────────┘
    │
    │ 4. Call FileService
    │
┌───▼────────────────┐
│   FileService      │
│  uploadFile()      │
└───┬────────────────┘
    │
    │ 5. Create user directory
    │ 6. Check for duplicates
    │ 7. Copy file to storage/
    │
┌───▼────────────────┐
│  File System       │
│ storage/user_X/    │
└───┬────────────────┘
    │
    │ 8. File copied
    │
┌───▼────────────────┐
│   FileService      │
│ Create FileItem    │
└───┬────────────────┘
    │
    │ 9. Save metadata
    │
┌───▼────────────────┐
│     FileDAO        │
│   saveFile()       │
└───┬────────────────┘
    │
    │ 10. Insert into DB
    │
┌───▼────────────────┐
│  MySQL Database    │
│   files table      │
└───┬────────────────┘
    │
    │ 11. Return FileItem with ID
    │
┌───▼────────────────┐
│ DashboardController│
│ Show success alert │
│ Refresh file list  │
│ Update stats       │
└────────────────────┘
```

### 3. File Search Flow

```
┌──────┐
│ User │
└───┬──┘
    │
    │ 1. Type in search bar
    │
┌───▼────────────────┐
│ DashboardController│
│  handleSearch()    │
└───┬────────────────┘
    │
    │ 2. Get search term
    │
┌───▼────────────────┐
│   FileService      │
│  searchFiles()     │
└───┬────────────────┘
    │
    │ 3. Query with LIKE
    │
┌───▼────────────────┐
│     FileDAO        │
│  searchFiles()     │
└───┬────────────────┘
    │
    │ 4. Execute SQL
    │    SELECT * FROM files
    │    WHERE user_id = ?
    │    AND filename LIKE ?
    │
┌───▼────────────────┐
│  MySQL Database    │
│   files table      │
└───┬────────────────┘
    │
    │ 5. Return matching files
    │
┌───▼────────────────┐
│ DashboardController│
│ Update table view  │
│ Filter results     │
└────────────────────┘
```

## 📦 Package Structure

```
com.patrakosh
│
├── controller/              # UI Controllers
│   ├── LoginController      # Login screen logic
│   ├── SignupController     # Signup screen logic
│   └── DashboardController  # Dashboard logic
│
├── service/                 # Business Logic
│   ├── AuthService          # Authentication
│   └── FileService          # File operations
│
├── dao/                     # Data Access
│   ├── UserDAO              # User CRUD
│   └── FileDAO              # File CRUD
│
├── model/                   # Data Models
│   ├── User                 # User entity
│   └── FileItem             # File entity
│
├── util/                    # Utilities
│   ├── Config               # Config loader
│   ├── DBUtil               # DB connection
│   └── FileUtil             # File helpers
│
├── MainApp                  # JavaFX App
└── Launcher                 # Entry point
```

## 🔐 Security Architecture

```
┌─────────────────────────────────────────┐
│         Security Layers                 │
└─────────────────────────────────────────┘

1. INPUT VALIDATION
   ├── Client-side (JavaFX Controllers)
   │   ├── Empty field checks
   │   ├── Email format validation
   │   └── Password length validation
   │
   └── Server-side (Service Layer)
       ├── Business rule validation
       ├── Duplicate username check
       └── Duplicate email check

2. PASSWORD SECURITY
   ├── SHA-256 Hashing
   ├── No plain text storage
   └── Minimum length requirement

3. SQL INJECTION PREVENTION
   ├── Prepared Statements
   ├── Parameterized Queries
   └── No string concatenation

4. FILE SECURITY
   ├── User-specific directories
   ├── Path validation
   └── Access control by user_id

5. SESSION MANAGEMENT
   ├── In-memory user object
   ├── Logout clears session
   └── No persistent sessions
```

## 🗄️ Database Design

### Entity Relationship Diagram

```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ PK  id              │
│     username        │
│     email           │
│     password        │
│     created_at      │
└──────────┬──────────┘
           │
           │ 1
           │
           │ has many
           │
           │ N
           │
┌──────────▼──────────┐
│       files         │
├─────────────────────┤
│ PK  id              │
│ FK  user_id         │
│     filename        │
│     filepath        │
│     file_size       │
│     upload_time     │
└─────────────────────┘

Relationship: One-to-Many
- One user can have many files
- Each file belongs to one user
- CASCADE DELETE: Deleting user deletes their files
```

### Database Indexes

```sql
-- Primary Keys (Auto-indexed)
users.id
files.id

-- Foreign Keys (Auto-indexed)
files.user_id

-- Custom Indexes
CREATE INDEX idx_user_id ON files(user_id);
CREATE INDEX idx_upload_time ON files(upload_time);

Purpose:
- idx_user_id: Fast file lookup by user
- idx_upload_time: Fast sorting by date
```

## 🎨 UI Component Hierarchy

```
MainApp (JavaFX Application)
│
├── Login Scene
│   └── StackPane (Root)
│       └── VBox (Card)
│           ├── Label (Logo)
│           ├── Label (Title)
│           ├── TextField (Username)
│           ├── HBox (Password + Toggle)
│           ├── Label (Error)
│           ├── Button (Login)
│           └── Hyperlink (Signup)
│
├── Signup Scene
│   └── StackPane (Root)
│       └── VBox (Card)
│           ├── Label (Logo)
│           ├── Label (Title)
│           ├── TextField (Username)
│           ├── TextField (Email)
│           ├── PasswordField (Password)
│           ├── PasswordField (Confirm)
│           ├── Label (Error/Success)
│           ├── Button (Signup)
│           └── Hyperlink (Login)
│
└── Dashboard Scene
    └── BorderPane (Root)
        ├── Top: HBox (Navigation)
        │   ├── Label (Logo)
        │   ├── Label (Welcome)
        │   └── MenuButton (User Menu)
        │
        ├── Center: VBox (Main Content)
        │   ├── HBox (Action Bar)
        │   │   ├── Button (Upload)
        │   │   ├── Button (Refresh)
        │   │   └── TextField (Search)
        │   │
        │   ├── HBox (Stats Cards)
        │   │   ├── VBox (Total Files)
        │   │   └── VBox (Storage Used)
        │   │
        │   └── VBox (File Table Container)
        │       └── TableView (Files)
        │           ├── TableColumn (Icon)
        │           ├── TableColumn (Filename)
        │           ├── TableColumn (Size)
        │           ├── TableColumn (Date)
        │           └── TableColumn (Actions)
        │
        └── Bottom: HBox (Status Bar)
            └── Label (Status)
```

## 🔄 State Management

```
Application State Flow:

┌──────────────┐
│  No User     │  Initial State
│  (Logged Out)│
└──────┬───────┘
       │
       │ Login Success
       │
┌──────▼───────┐
│  User Set    │  MainApp.setCurrentUser(user)
│  (Logged In) │
└──────┬───────┘
       │
       │ Using Application
       │
┌──────▼───────┐
│  Dashboard   │  File operations, search, etc.
│  Active      │
└──────┬───────┘
       │
       │ Logout
       │
┌──────▼───────┐
│  User Null   │  MainApp.setCurrentUser(null)
│  (Logged Out)│
└──────────────┘

State Storage:
- Current User: MainApp.currentUser (static)
- File List: DashboardController.filesList (ObservableList)
- UI State: Controller instance variables
```

## 🧵 Threading Model

```
Main Thread (JavaFX Application Thread)
│
├── UI Rendering
├── Event Handling
└── Scene Management

Background Threads (Worker Threads)
│
├── Database Operations
│   ├── User queries
│   └── File queries
│
├── File Operations
│   ├── Upload (copy file)
│   ├── Download (copy file)
│   └── Delete (remove file)
│
└── Authentication
    ├── Password hashing
    └── User validation

Thread Communication:
- Platform.runLater() for UI updates
- new Thread() for background tasks
- Synchronization via JavaFX properties
```

## 📊 Performance Considerations

### Database Connection Pooling
```
Current: Single connection (DBUtil.getConnection())
Future: Connection pool for better performance
```

### File Operations
```
- Async file upload/download
- Progress indicators for large files
- Chunked reading for memory efficiency
```

### UI Responsiveness
```
- Background threads for I/O
- Platform.runLater() for UI updates
- Disabled buttons during operations
```

## 🔧 Configuration Management

```
application.properties
├── Database Configuration
│   ├── db.url
│   ├── db.username
│   ├── db.password
│   └── db.driver
│
└── Application Configuration
    ├── app.name
    ├── app.version
    └── storage.base.path

Loaded by: Config.java (static initialization)
Access via: Config.get(key)
```

## 🎯 Design Patterns Used

1. **MVC (Model-View-Controller)**
   - View: FXML files
   - Controller: Controller classes
   - Model: User, FileItem

2. **DAO (Data Access Object)**
   - UserDAO, FileDAO
   - Abstracts database operations

3. **Service Layer**
   - AuthService, FileService
   - Business logic separation

4. **Singleton**
   - DBUtil (single connection)
   - Config (static properties)

5. **Observer**
   - ObservableList for table updates
   - JavaFX property binding

---

**This architecture ensures:**
- ✅ Separation of concerns
- ✅ Maintainability
- ✅ Scalability
- ✅ Testability
- ✅ Security
- ✅ Performance
