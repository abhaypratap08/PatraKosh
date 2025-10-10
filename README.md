# PatraKosh - Centralized File Repository System

**PatraKosh** is a modern, desktop file storage solution inspired by Dropbox, built with **JavaFX** and **MySQL**. Enjoy a beautiful, intuitive interface for uploading, downloading, searching, and securely managing your files.

---

## 📚 Documentation

### Quick Links
- 🚀 [Quick Start Guide](#-quick-start-3-simple-steps) - Get started in 3 steps
- 📖 [Full Documentation](#-complete-documentation-index) - All guides and references
- 🔧 [Troubleshooting](#-troubleshooting) - Common issues and solutions
- 👥 [Contributing](#-contributing) - How to contribute

### 📖 Complete Documentation Index

| Document | Description | Link |
|----------|-------------|------|
| **QUICK_START.md** | 3-step beginner guide | [View Guide](QUICK_START.md) |
| **GETTING_STARTED.md** | Detailed installation walkthrough | [View Guide](GETTING_STARTED.md) |
| **SETUP_CHECKLIST.md** | Step-by-step verification checklist | [View Checklist](SETUP_CHECKLIST.md) |
| **GITHUB_UPLOAD_GUIDE.md** | Guide for uploading to GitHub | [View Guide](GITHUB_UPLOAD_GUIDE.md) |
| **PROJECT_SUMMARY.md** | Complete project overview | [View Summary](PROJECT_SUMMARY.md) |
| **ARCHITECTURE.md** | Technical architecture details | [View Architecture](ARCHITECTURE.md) |
| **QUICKSTART.md** | 5-minute setup guide | [View Guide](QUICKSTART.md) |

---

## 🚀 Quick Start (3 Simple Steps)

### Step 1: Download PatraKosh

**Option A: Download ZIP (Easiest)**
1. Go to: https://github.com/abhaypratap08/PatraKosh
2. Click the green **"Code"** button
3. Click **"Download ZIP"**
4. Extract the ZIP file to your desired location
5. Open the extracted folder

**Option B: Clone with Git**
```bash
git clone https://github.com/abhaypratap08/PatraKosh.git
cd PatraKosh
```

### Step 2: Setup Database (One-Time)

**Double-click:** `setup-database.bat`

This will automatically:
- ✅ Find your MySQL installation
- ✅ Create the `patrakosh_db` database
- ✅ Create all required tables
- ✅ Configure the application

**What you need:**
- MySQL Server installed ([Download here](https://dev.mysql.com/downloads/mysql/))
- Your MySQL root password ready

### Step 3: Run PatraKosh

**Double-click:** `runApp.bat`

Then choose **Option 1** to start the application!

---

## ✨ Features

### User Authentication
- **Secure Login System:** Username/email and password authentication
- **User Registration:** Create new accounts with validation
- **Password Security:** SHA-256 password hashing
- **Session Management:** Robust session handling across the application

### File Management
- **Upload Files:** Drag-and-drop file uploads
- **Download Files:** Download any file to your system
- **Delete Files:** Remove files with confirmation
- **Search Functionality:** Instantly search files by name
- **File Metadata:** Track size, upload date, and more

### Modern UI
- **Google Pixel-inspired Design:** Clean, modern interface
- **Responsive Layout:** Adapts to different window sizes
- **Visual Feedback:** Hover effects, animations, and status updates
- **File Icons:** Visual file type indicators
- **Statistics Dashboard:** View total files and storage usage

---

## 📋 Prerequisites

Before running PatraKosh, ensure you have:

- ✅ **Java 17+** - [Download here](https://adoptium.net/)
- ✅ **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- ✅ **MySQL 8.0+** - [Download here](https://dev.mysql.com/downloads/mysql/)

**Don't have these installed?** See our [detailed installation guide](GETTING_STARTED.md).

---

## 🛠️ Manual Installation (Advanced Users)

If you prefer manual setup or the automated scripts don't work:

### 1. Clone the Repository
```bash
git clone https://github.com/abhaypratap08/PatraKosh.git
cd PatraKosh
```

### 2. Set Up MySQL Database
```bash
mysql -u root -p < database_setup.sql
```

Or manually:
```sql
CREATE DATABASE patrakosh_db;
USE patrakosh_db;
SOURCE database_setup.sql;
```

### 3. Configure Database Connection

Edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/patrakosh_db
db.username=root
db.password=YOUR_MYSQL_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application

**Option A: Using Maven**
```bash
mvn javafx:run
```

**Option B: Using JAR**
```bash
java -jar target/patrakosh-1.0.0.jar
```

**Option C: From IDE**
Run the `Launcher.java` class as a Java application

---

## 📁 Project Structure

```
PatraKosh/
├── src/main/
│   ├── java/com/patrakosh/
│   │   ├── controller/          # UI Controllers
│   │   │   ├── LoginController.java
│   │   │   ├── SignupController.java
│   │   │   └── DashboardController.java
│   │   ├── dao/                 # Data Access Objects
│   │   │   ├── UserDAO.java
│   │   │   └── FileDAO.java
│   │   ├── model/               # Data Models
│   │   │   ├── User.java
│   │   │   └── FileItem.java
│   │   ├── service/             # Business Logic
│   │   │   ├── AuthService.java
│   │   │   └── FileService.java
│   │   ├── util/                # Utilities
│   │   │   ├── Config.java
│   │   │   ├── DBUtil.java
│   │   │   └── FileUtil.java
│   │   ├── Launcher.java        # Application Entry Point
│   │   └── MainApp.java         # JavaFX Application
│   └── resources/
│       ├── fxml/                # UI Layouts
│       │   ├── login.fxml
│       │   ├── signup.fxml
│       │   └── dashboard.fxml
│       └── application.properties
├── storage/                     # File Storage Directory
├── database_setup.sql           # Database Schema
├── setup-database.bat           # Automated Database Setup
├── runApp.bat                   # Application Launcher
├── pom.xml                      # Maven Configuration
└── README.md                    # This file
```

For detailed architecture, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 🔧 Technologies Used

- **Java 17** - Core programming language
- **JavaFX 21** - UI framework
- **MySQL 9.0** - Database
- **Maven** - Build and dependency management
- **Apache Commons IO** - File operations
- **SLF4J** - Logging

---

## 📝 Usage Guide

### Creating an Account
1. Launch the application
2. Click "Sign up" on the login screen
3. Fill in username, email, and password
4. Click "Sign Up"
5. After successful registration, you'll be redirected to login

### Logging In
1. Enter your username or email
2. Enter your password
3. Click "Login"

### Uploading Files
1. Click the "⬆ Upload File" button
2. Select a file from your computer
3. Wait for the upload to complete
4. File will appear in the table

### Downloading Files
1. Find the file in the table
2. Click the download button (⬇)
3. Choose where to save the file
4. File will be downloaded to the selected location

### Deleting Files
1. Find the file in the table
2. Click the delete button (🗑)
3. Confirm the deletion
4. File will be removed from storage

### Searching Files
1. Type the filename in the search bar
2. Results will filter automatically

---

## 🔐 Security Features

- **Password Hashing:** SHA-256 encryption for stored passwords
- **SQL Injection Prevention:** Prepared statements in all database queries
- **Input Validation:** Client-side and server-side validation
- **Session Management:** Secure user session handling
- **File Isolation:** Each user has separate storage directory

---

## 🐛 Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `net start mysql`
- Check database credentials in `application.properties`
- Ensure `patrakosh_db` database exists
- Run `setup-database.bat` to recreate database

### JavaFX Module Errors
- Ensure Java 17+ is installed: `java -version`
- Use the `Launcher.java` class as the main entry point
- Verify Maven dependencies are downloaded: `mvn clean install`

### File Upload/Download Issues
- Check file permissions
- Ensure `storage/` directory exists and is writable
- Verify sufficient disk space

### Application Won't Start
- Check Java version (must be 17+)
- Check Maven version (must be 3.6+)
- Rebuild project: `mvn clean install`
- Check console for error messages

For more troubleshooting, see [SETUP_CHECKLIST.md](SETUP_CHECKLIST.md).

---

## 📦 Building Executable JAR

To create a standalone executable JAR:

```bash
mvn clean package
```

The JAR file will be created in `target/patrakosh-1.0.0.jar`

Run it with:
```bash
java -jar target/patrakosh-1.0.0.jar
```

---

## 🤝 Contributing

This is a team project by **TeamAlgoNauts**. Contributions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

For uploading to GitHub, see [GITHUB_UPLOAD_GUIDE.md](GITHUB_UPLOAD_GUIDE.md).

---

## 📄 License

This project is created for educational purposes.

---

## 👥 Team

**TeamAlgoNauts**

---

## 📞 Support

For issues or questions:
- Create an issue: [GitHub Issues](https://github.com/abhaypratap08/PatraKosh/issues)
- Check documentation: See [Documentation Index](#-complete-documentation-index)
- Review troubleshooting: See [Troubleshooting](#-troubleshooting)

---

## 🎯 Additional Resources

- **Quick Start:** [QUICK_START.md](QUICK_START.md) - 3-step guide
- **Detailed Setup:** [GETTING_STARTED.md](GETTING_STARTED.md) - Complete walkthrough
- **Verification:** [SETUP_CHECKLIST.md](SETUP_CHECKLIST.md) - Step-by-step checklist
- **Project Overview:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Complete summary
- **Architecture:** [ARCHITECTURE.md](ARCHITECTURE.md) - Technical details

---

> **⚠️ Important Note:**  
> Don't forget to update your MySQL credentials in `application.properties` before running PatraKosh!
> 
> The automated `setup-database.bat` script will do this for you automatically.

---

**Built with ❤️ by TeamAlgoNauts** 🚀
