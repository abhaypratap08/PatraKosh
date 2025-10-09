# PatraKosh - Setup Checklist ✅

Follow this checklist to ensure everything is configured correctly before running the application.

## Prerequisites Check

- [ ] **Java 17 or higher installed**
  ```bash
  java -version
  ```
  Expected output: `java version "17.x.x"` or higher

- [ ] **Maven 3.6+ installed**
  ```bash
  mvn -version
  ```
  Expected output: `Apache Maven 3.6.x` or higher

- [ ] **MySQL 8.0+ installed and running**
  - Check if MySQL service is running
  - Can connect to MySQL via command line or Workbench

## Database Setup

- [ ] **Step 1: Create Database**
  ```sql
  CREATE DATABASE IF NOT EXISTS patrakosh_db;
  ```

- [ ] **Step 2: Run Database Schema**
  - Option A: Run `database_setup.sql` file
  - Option B: Copy-paste SQL from file into MySQL

- [ ] **Step 3: Verify Tables Created**
  ```sql
  USE patrakosh_db;
  SHOW TABLES;
  ```
  Expected output: `users` and `files` tables

## Configuration

- [ ] **Update Database Credentials**
  - Open: `src/main/resources/application.properties`
  - Update `db.username` (default: root)
  - Update `db.password` (your MySQL password)
  - Update `db.url` if using different host/port

- [ ] **Verify Configuration**
  ```properties
  db.url=jdbc:mysql://localhost:3306/patrakosh_db
  db.username=YOUR_USERNAME
  db.password=YOUR_PASSWORD
  ```

## Build & Dependencies

- [ ] **Download Maven Dependencies**
  ```bash
  mvn clean install
  ```
  - This may take a few minutes on first run
  - Should complete with "BUILD SUCCESS"

- [ ] **Verify Dependencies Downloaded**
  - Check `target/` folder exists
  - No error messages in console

## First Run

- [ ] **Run the Application**
  ```bash
  mvn javafx:run
  ```
  OR double-click `run.bat` (Windows)

- [ ] **Verify Application Starts**
  - Login window appears
  - No error dialogs
  - UI looks correct

## Test Basic Functionality

- [ ] **Create Test Account**
  - Click "Sign up"
  - Enter username: `testuser`
  - Enter email: `test@example.com`
  - Enter password: `test123`
  - Confirm password: `test123`
  - Click "Sign Up"
  - Success message appears

- [ ] **Login with Test Account**
  - Enter username: `testuser`
  - Enter password: `test123`
  - Click "Login"
  - Dashboard appears

- [ ] **Upload Test File**
  - Click "⬆ Upload File"
  - Select any file
  - File appears in table
  - Stats update (Total Files: 1)

- [ ] **Download Test File**
  - Click ⬇ button next to file
  - Choose save location
  - File downloads successfully

- [ ] **Search Test**
  - Type filename in search bar
  - File filters correctly

- [ ] **Delete Test File**
  - Click 🗑 button
  - Confirm deletion
  - File removed from table
  - Stats update (Total Files: 0)

- [ ] **Logout**
  - Click 👤 menu
  - Click "Logout"
  - Returns to login screen

## Troubleshooting

### ❌ "Cannot connect to database"
**Fix:**
- [ ] MySQL service is running
- [ ] Database `patrakosh_db` exists
- [ ] Credentials in `application.properties` are correct
- [ ] MySQL is on port 3306 (or update URL)

### ❌ "Module not found" or JavaFX errors
**Fix:**
- [ ] Java 17+ is installed (not Java 8 or 11)
- [ ] Using Maven to run: `mvn javafx:run`
- [ ] Maven dependencies downloaded successfully

### ❌ "BUILD FAILURE" during mvn install
**Fix:**
- [ ] Internet connection active (Maven downloads dependencies)
- [ ] Check Maven settings.xml if behind proxy
- [ ] Delete `~/.m2/repository` and retry

### ❌ Files not uploading
**Fix:**
- [ ] `storage/` directory exists (auto-created)
- [ ] Write permissions on project directory
- [ ] Sufficient disk space available

### ❌ UI looks broken or misaligned
**Fix:**
- [ ] Window size is at least 900x600
- [ ] JavaFX 21 is being used (check pom.xml)
- [ ] FXML files are in correct location

## Performance Check

- [ ] **Login Response Time**: < 2 seconds
- [ ] **File Upload Speed**: Reasonable for file size
- [ ] **Table Loads**: < 1 second for 100 files
- [ ] **Search Response**: Instant filtering

## Security Verification

- [ ] **Password Hashing**: Passwords not stored in plain text in DB
  ```sql
  SELECT password FROM users LIMIT 1;
  ```
  Should show long hash string, not plain password

- [ ] **File Isolation**: Each user has separate folder
  - Check `storage/user_1/`, `storage/user_2/`, etc.

- [ ] **SQL Injection Test**: Try entering `' OR '1'='1` in login
  - Should fail gracefully, not cause error

## Optional Enhancements

- [ ] Add application icon (uncomment in MainApp.java)
- [ ] Create desktop shortcut to `run.bat`
- [ ] Set up IDE run configuration
- [ ] Enable auto-login (remember me feature)
- [ ] Add file preview functionality

## Final Verification

- [ ] All 14 Java files compile without errors
- [ ] All 3 FXML files load correctly
- [ ] Database has 2 tables with proper schema
- [ ] Can create account, login, upload, download, delete files
- [ ] Application runs without console errors
- [ ] UI is responsive and looks modern

## Success Criteria ✅

Your setup is complete when:
1. ✅ Application starts without errors
2. ✅ Can create and login to account
3. ✅ Can upload, download, and delete files
4. ✅ Search functionality works
5. ✅ Stats display correctly
6. ✅ No database connection errors

---

## Quick Commands Reference

```bash
# Build project
mvn clean install

# Run application
mvn javafx:run

# Create JAR
mvn clean package

# Run JAR
java -jar target/patrakosh-1.0.0.jar

# Check Java version
java -version

# Check Maven version
mvn -version
```

## Support

If you encounter issues not covered here:
1. Check `README.md` for detailed documentation
2. Review `QUICKSTART.md` for quick setup
3. Examine `PROJECT_STRUCTURE.md` for architecture details

---

**Ready to go? Run `mvn javafx:run` and start using PatraKosh! 🚀**
