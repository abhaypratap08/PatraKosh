# PatraKosh - Project Structure

## 📂 Complete File Tree

```
TeamAlgoNauts-PatraKosh/
│
├── src/
│   └── main/
│       ├── java/com/patrakosh/
│       │   │
│       │   ├── controller/                    # UI Controllers (MVC Pattern)
│       │   │   ├── LoginController.java       # Handles login screen logic
│       │   │   ├── SignupController.java      # Handles signup screen logic
│       │   │   └── DashboardController.java   # Handles dashboard and file operations
│       │   │
│       │   ├── dao/                           # Data Access Objects (Database Layer)
│       │   │   ├── UserDAO.java               # User database operations (CRUD)
│       │   │   └── FileDAO.java               # File metadata database operations
│       │   │
│       │   ├── model/                         # Data Models (POJOs)
│       │   │   ├── User.java                  # User entity (id, username, email, password)
│       │   │   └── FileItem.java              # File entity (id, filename, size, upload_time)
│       │   │
│       │   ├── service/                       # Business Logic Layer
│       │   │   ├── AuthService.java           # Authentication & user management
│       │   │   └── FileService.java           # File upload/download/delete operations
│       │   │
│       │   ├── util/                          # Utility Classes
│       │   │   ├── Config.java                # Configuration properties loader
│       │   │   ├── DBUtil.java                # Database connection manager
│       │   │   └── FileUtil.java              # File formatting utilities
│       │   │
│       │   ├── Launcher.java                  # Main entry point (non-JavaFX class)
│       │   └── MainApp.java                   # JavaFX Application class
│       │
│       └── resources/
│           ├── fxml/                          # FXML UI Layouts
│           │   ├── login.fxml                 # Login screen UI
│           │   ├── signup.fxml                # Signup screen UI
│           │   └── dashboard.fxml             # Dashboard screen UI
│           │
│           └── application.properties         # Database & app configuration
│
├── storage/                                   # File storage directory (auto-created)
│   └── user_[id]/                            # Per-user storage folders
│
├── target/                                    # Maven build output (auto-generated)
│
├── pom.xml                                    # Maven project configuration
├── database_setup.sql                         # MySQL database schema
├── README.md                                  # Full documentation
├── QUICKSTART.md                              # Quick start guide
├── PROJECT_STRUCTURE.md                       # This file
├── .gitignore                                 # Git ignore rules
└── run.bat                                    # Windows batch script to run app
```

## 🏗️ Architecture Overview

### MVC Pattern Implementation

```
┌─────────────┐
│    View     │  FXML Files (login.fxml, signup.fxml, dashboard.fxml)
│  (FXML UI)  │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ Controller  │  LoginController, SignupController, DashboardController
│   (Logic)   │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│  Service    │  AuthService, FileService
│ (Business)  │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│     DAO     │  UserDAO, FileDAO
│  (Database) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│   Model     │  User, FileItem
│   (Data)    │
└─────────────┘
```

## 📋 File Descriptions

### Core Application Files

| File | Purpose | Lines |
|------|---------|-------|
| `Launcher.java` | Application entry point (avoids JavaFX module issues) | ~10 |
| `MainApp.java` | JavaFX application, scene management | ~80 |

### Controllers (UI Logic)

| File | Purpose | Key Methods |
|------|---------|-------------|
| `LoginController.java` | Login screen logic | `handleLogin()`, `togglePasswordVisibility()` |
| `SignupController.java` | Signup screen logic | `handleSignup()`, validation methods |
| `DashboardController.java` | Dashboard & file operations | `handleUpload()`, `handleDownload()`, `handleDelete()` |

### Service Layer (Business Logic)

| File | Purpose | Key Methods |
|------|---------|-------------|
| `AuthService.java` | User authentication | `login()`, `signup()`, `hashPassword()` |
| `FileService.java` | File management | `uploadFile()`, `downloadFile()`, `deleteFile()` |

### DAO Layer (Database)

| File | Purpose | Key Methods |
|------|---------|-------------|
| `UserDAO.java` | User database operations | `createUser()`, `getUserByUsername()`, `getUserByEmail()` |
| `FileDAO.java` | File metadata operations | `saveFile()`, `getFilesByUserId()`, `deleteFile()` |

### Models (Data Entities)

| File | Purpose | Properties |
|------|---------|-----------|
| `User.java` | User entity | id, username, email, password, createdAt |
| `FileItem.java` | File entity | id, userId, filename, filepath, fileSize, uploadTime |

### Utilities

| File | Purpose | Key Methods |
|------|---------|-------------|
| `Config.java` | Load configuration | `getDbUrl()`, `getDbUsername()`, `getStorageBasePath()` |
| `DBUtil.java` | Database connection | `getConnection()`, `closeConnection()` |
| `FileUtil.java` | File utilities | `formatFileSize()`, `formatTimeAgo()`, `getFileIcon()` |

### FXML Files (UI Layouts)

| File | Description | Components |
|------|-------------|-----------|
| `login.fxml` | Login screen layout | Username field, password field, login button |
| `signup.fxml` | Signup screen layout | Username, email, password, confirm password fields |
| `dashboard.fxml` | Dashboard layout | File table, upload button, search bar, stats |

## 🔄 Data Flow Example: File Upload

```
1. User clicks "Upload File" button
   ↓
2. DashboardController.handleUpload()
   ↓
3. FileChooser dialog opens
   ↓
4. User selects file
   ↓
5. FileService.uploadFile(userId, file)
   ↓
6. Copy file to storage/user_[id]/
   ↓
7. FileDAO.saveFile(fileItem)
   ↓
8. Insert metadata into MySQL
   ↓
9. Return FileItem object
   ↓
10. Update UI (refresh table, update stats)
```

## 🗄️ Database Schema

### Users Table
```sql
users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### Files Table
```sql
files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    filepath VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

## 📦 Maven Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| JavaFX Controls | 21.0.4 | UI components |
| JavaFX FXML | 21.0.4 | FXML support |
| MySQL Connector | 9.0.0 | Database connectivity |
| Commons IO | 2.16.1 | File operations |
| SLF4J Simple | 2.0.13 | Logging |

## 🎨 UI Design Specifications

### Color Palette
- **Primary**: #2196F3 (Blue)
- **Success**: #4CAF50 (Green)
- **Error**: #F44336 (Red)
- **Background**: #F5F5F5 (Light Gray)
- **Cards**: #FFFFFF (White)
- **Text**: #424242 (Dark Gray)
- **Secondary Text**: #757575 (Gray)

### Typography
- **Headers**: Bold, 18-24px
- **Body**: Regular, 14px
- **Small**: 12-13px

### Spacing
- **Card Padding**: 40px
- **Element Spacing**: 20px
- **Border Radius**: 8-12px

## 🔐 Security Features

1. **Password Hashing**: SHA-256 encryption
2. **SQL Injection Prevention**: Prepared statements
3. **Input Validation**: Client and server-side
4. **Session Management**: User object in memory
5. **File Isolation**: Per-user storage directories

## 🚀 Build & Run Commands

```bash
# Clean and build
mvn clean install

# Run application
mvn javafx:run

# Create executable JAR
mvn clean package

# Run JAR
java -jar target/patrakosh-1.0.0.jar

# Windows quick run
run.bat
```

## 📊 Code Statistics

- **Total Java Files**: 15
- **Total FXML Files**: 3
- **Total Lines of Code**: ~2,500+
- **Packages**: 6 (controller, dao, model, service, util, root)
- **Classes**: 15
- **Interfaces**: 0 (using concrete classes)

## 🎯 Key Design Patterns

1. **MVC (Model-View-Controller)**: Separation of concerns
2. **DAO (Data Access Object)**: Database abstraction
3. **Service Layer**: Business logic separation
4. **Singleton**: Database connection management
5. **Factory**: Scene creation in MainApp

## 📝 Configuration Files

### application.properties
```properties
db.url=jdbc:mysql://localhost:3306/patrakosh_db
db.username=root
db.password=root
db.driver=com.mysql.cj.jdbc.Driver
app.name=PatraKosh
app.version=1.0.0
storage.base.path=storage
```

### pom.xml
- Maven project configuration
- Dependencies management
- Build plugins (compiler, javafx, shade)

## 🔧 Development Setup

1. **IDE**: IntelliJ IDEA / Eclipse / VS Code
2. **JDK**: Java 17+
3. **Build Tool**: Maven 3.6+
4. **Database**: MySQL 8.0+
5. **Version Control**: Git

---

**Project Created By**: TeamAlgoNauts  
**Technology Stack**: Java 17 + JavaFX 21 + MySQL 9.0 + Maven  
**Architecture**: MVC with Service & DAO Layers  
**UI Design**: Google Pixel-inspired Modern UI
