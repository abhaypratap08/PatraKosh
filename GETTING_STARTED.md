# Getting Started with PatraKosh

## 🎯 Welcome!

Welcome to **PatraKosh** - your personal desktop file storage solution! This guide will help you get up and running in just a few minutes.

## 📋 What You'll Need

Before starting, make sure you have:
- ✅ Windows PC (Windows 10 or later)
- ✅ 30 minutes of time
- ✅ Internet connection (for downloading dependencies)
- ✅ Administrator access (for MySQL installation)

## 🚀 Installation Steps

### Step 1: Install Java 17 (10 minutes)

1. **Download Java 17**
   - Visit: https://www.oracle.com/java/technologies/downloads/#java17
   - Download: Windows x64 Installer
   - File: `jdk-17_windows-x64_bin.exe`

2. **Install Java**
   - Run the installer
   - Click "Next" through all steps
   - Use default installation path

3. **Verify Installation**
   ```bash
   java -version
   ```
   Should show: `java version "17.x.x"`

### Step 2: Install Maven (5 minutes)

1. **Download Maven**
   - Visit: https://maven.apache.org/download.cgi
   - Download: Binary zip archive
   - File: `apache-maven-3.x.x-bin.zip`

2. **Extract Maven**
   - Extract to: `C:\Program Files\Apache\maven`

3. **Add to PATH**
   - Open: System Properties → Environment Variables
   - Edit: System Variable "Path"
   - Add: `C:\Program Files\Apache\maven\bin`

4. **Verify Installation**
   ```bash
   mvn -version
   ```
   Should show: `Apache Maven 3.x.x`

### Step 3: Install MySQL (10 minutes)

1. **Download MySQL**
   - Visit: https://dev.mysql.com/downloads/installer/
   - Download: MySQL Installer for Windows
   - File: `mysql-installer-community-x.x.x.msi`

2. **Install MySQL**
   - Run installer
   - Choose: "Developer Default"
   - Set root password: Remember this!
   - Complete installation

3. **Verify Installation**
   - Open: MySQL Workbench
   - Connect to: Local instance
   - Should connect successfully

### Step 4: Setup PatraKosh Database (2 minutes)

1. **Open MySQL Command Line** or **MySQL Workbench**

2. **Run Database Setup**
   ```sql
   CREATE DATABASE IF NOT EXISTS patrakosh_db;
   USE patrakosh_db;

   CREATE TABLE IF NOT EXISTS users (
       id INT PRIMARY KEY AUTO_INCREMENT,
       username VARCHAR(50) UNIQUE NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   CREATE TABLE IF NOT EXISTS files (
       id INT PRIMARY KEY AUTO_INCREMENT,
       user_id INT NOT NULL,
       filename VARCHAR(255) NOT NULL,
       filepath VARCHAR(500) NOT NULL,
       file_size BIGINT NOT NULL,
       upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
   );

   CREATE INDEX idx_user_id ON files(user_id);
   CREATE INDEX idx_upload_time ON files(upload_time);
   ```

3. **Verify Tables Created**
   ```sql
   SHOW TABLES;
   ```
   Should show: `users` and `files`

### Step 5: Configure PatraKosh (1 minute)

1. **Open Configuration File**
   - File: `src/main/resources/application.properties`

2. **Update Database Password**
   ```properties
   db.url=jdbc:mysql://localhost:3306/patrakosh_db
   db.username=root
   db.password=YOUR_MYSQL_PASSWORD_HERE
   db.driver=com.mysql.cj.jdbc.Driver
   ```

3. **Save the File**

### Step 6: Build and Run (2 minutes)

1. **Open Command Prompt**
   - Navigate to project folder
   ```bash
   cd C:\Users\prata\OneDrive\Desktop\TeamAlgoNauts-PatraKosh
   ```

2. **Build the Project**
   ```bash
   mvn clean install
   ```
   - Wait for "BUILD SUCCESS"
   - This downloads dependencies (first time only)

3. **Run the Application**
   ```bash
   mvn javafx:run
   ```
   OR double-click `run.bat`

4. **Application Launches!** 🎉

## 🎮 First Time Usage

### Create Your Account

1. **Signup Screen**
   - Click "Sign up" link on login screen
   
2. **Fill in Details**
   - Username: Choose a unique username
   - Email: Your email address
   - Password: At least 6 characters
   - Confirm Password: Same as password

3. **Click "Sign Up"**
   - Wait for success message
   - Auto-redirects to login

### Login to Your Account

1. **Login Screen**
   - Enter your username or email
   - Enter your password
   - Click "Login"

2. **Dashboard Appears!**
   - You're now in the main application

### Upload Your First File

1. **Click "⬆ Upload File" Button**
   - File chooser dialog opens

2. **Select a File**
   - Choose any file from your computer
   - Click "Open"

3. **File Uploads**
   - Button shows "Uploading..."
   - Success dialog appears
   - File appears in table!

### Download a File

1. **Find Your File in Table**
   - Locate the file you want

2. **Click Download Button (⬇)**
   - Green button on the right

3. **Choose Save Location**
   - Select where to save
   - Click "Save"

4. **File Downloaded!**
   - Success message appears

### Search for Files

1. **Type in Search Bar**
   - Located at top right

2. **Results Filter Instantly**
   - Table shows matching files

3. **Clear Search**
   - Delete text to show all files

### Delete a File

1. **Click Delete Button (🗑)**
   - Red button on the right

2. **Confirm Deletion**
   - Click "OK" in confirmation dialog

3. **File Removed**
   - Success message appears
   - Table updates

## 🎨 Understanding the Interface

### Login Screen
```
┌─────────────────────────┐
│         📦              │
│      PatraKosh          │
│                         │
│  [Username/Email]       │
│  [Password] 👁          │
│                         │
│    [Login Button]       │
│                         │
│  Don't have account?    │
│      Sign up            │
└─────────────────────────┘
```

### Dashboard
```
┌─────────────────────────────────────────┐
│ 📦 PatraKosh    Welcome, User    👤▼   │
├─────────────────────────────────────────┤
│                                         │
│ ⬆ Upload  🔄  [Search...] 🔍          │
│                                         │
│ ┌─────────────┐  ┌─────────────┐      │
│ │Total Files  │  │Storage Used │      │
│ │     5       │  │   2.5 MB    │      │
│ └─────────────┘  └─────────────┘      │
│                                         │
│ Your Files                              │
│ ┌─────────────────────────────────────┐│
│ │Icon│Filename  │Size│Date  │Actions││
│ ├────┼──────────┼────┼──────┼───────┤│
│ │📄 │doc.pdf   │1MB │2h ago│⬇ 🗑  ││
│ │🖼️ │pic.jpg   │500K│1d ago│⬇ 🗑  ││
│ └─────────────────────────────────────┘│
├─────────────────────────────────────────┤
│ Ready                                   │
└─────────────────────────────────────────┘
```

## 💡 Tips & Tricks

### Keyboard Shortcuts
- **Tab**: Move between fields
- **Enter**: Submit forms
- **Escape**: Close dialogs

### File Management
- **Duplicate Files**: Auto-renamed with _1, _2, etc.
- **Any File Type**: Upload any file format
- **File Icons**: Different icons for different types
- **Sort Files**: Click column headers

### Search Tips
- **Instant Search**: Results appear as you type
- **Case Insensitive**: Finds files regardless of case
- **Partial Match**: Finds "doc" in "document.pdf"

### Best Practices
- **Strong Passwords**: Use at least 8 characters
- **Regular Backups**: Download important files
- **Organize Files**: Use descriptive filenames
- **Clean Up**: Delete old files you don't need

## 🐛 Troubleshooting

### "Cannot connect to database"
**Problem**: Application can't reach MySQL

**Solutions**:
1. Check MySQL is running
   - Open Services → MySQL80 → Start
2. Verify password in `application.properties`
3. Check database exists: `SHOW DATABASES;`

### "Module not found" error
**Problem**: JavaFX modules not found

**Solutions**:
1. Verify Java 17+ is installed: `java -version`
2. Use Maven to run: `mvn javafx:run`
3. Don't run MainApp.java directly, use Launcher.java

### "BUILD FAILURE"
**Problem**: Maven can't build project

**Solutions**:
1. Check internet connection
2. Delete `.m2/repository` folder
3. Run `mvn clean install` again

### Files not uploading
**Problem**: Upload button doesn't work

**Solutions**:
1. Check disk space available
2. Verify write permissions
3. Check console for errors

### Application won't start
**Problem**: Window doesn't appear

**Solutions**:
1. Check Java version: Must be 17+
2. Check Maven version: Must be 3.6+
3. Rebuild: `mvn clean install`
4. Check console for error messages

## 📞 Getting Help

### Documentation
1. **Quick Setup**: Read `QUICKSTART.md`
2. **Detailed Guide**: Read `README.md`
3. **Troubleshooting**: Read `SETUP_CHECKLIST.md`
4. **Architecture**: Read `ARCHITECTURE.md`

### Common Questions

**Q: Can I change the storage location?**
A: Yes, edit `storage.base.path` in `application.properties`

**Q: Can multiple users use the same database?**
A: Yes! Each user has isolated storage and files

**Q: Is my data secure?**
A: Yes, passwords are hashed and files are isolated

**Q: Can I upload large files?**
A: Yes, no size limit (depends on disk space)

**Q: Can I access files from another computer?**
A: Not yet, this is a local application

## 🎯 Next Steps

### After Setup
1. ✅ Create your account
2. ✅ Upload some test files
3. ✅ Try searching
4. ✅ Download a file
5. ✅ Explore the interface

### Learn More
- Read `FEATURES.md` for complete feature list
- Read `ARCHITECTURE.md` to understand the code
- Read `PROJECT_STRUCTURE.md` for file organization

### Customize
- Change colors in FXML files
- Modify storage location
- Add new features
- Extend functionality

## ✅ Success Checklist

Before you're done, verify:
- [ ] Java 17 installed and working
- [ ] Maven installed and working
- [ ] MySQL installed and running
- [ ] Database created with tables
- [ ] Configuration file updated
- [ ] Application builds successfully
- [ ] Application runs without errors
- [ ] Can create account
- [ ] Can login
- [ ] Can upload files
- [ ] Can download files
- [ ] Can search files
- [ ] Can delete files

## 🎉 You're All Set!

Congratulations! You've successfully set up PatraKosh. 

**Start using your personal file storage system now!**

### Quick Commands
```bash
# Run application
mvn javafx:run

# Or use batch file
run.bat

# Rebuild if needed
mvn clean install
```

---

**Need help? Check the other documentation files!**

**Enjoy PatraKosh! 📦**

**Built by TeamAlgoNauts**
