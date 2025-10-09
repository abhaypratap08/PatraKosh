# PatraKosh - Quick Start Guide

## ⚡ Quick Setup (5 Minutes)

### Step 1: Setup Database (2 minutes)

1. Open MySQL Command Line or MySQL Workbench
2. Run these commands:

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

### Step 2: Configure Database Connection (1 minute)

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/patrakosh_db
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### Step 3: Build & Run (2 minutes)

Open terminal in project directory and run:

```bash
mvn clean install
mvn javafx:run
```

## 🎯 First Time Usage

1. **Create Account**
   - Click "Sign up" on login screen
   - Enter username, email, and password (min 6 characters)
   - Click "Sign Up"

2. **Login**
   - Enter your username or email
   - Enter your password
   - Click "Login"

3. **Upload Your First File**
   - Click "⬆ Upload File" button
   - Select any file from your computer
   - File appears in the table!

## 🔧 Common Issues & Solutions

### Issue: "Cannot connect to database"
**Solution**: 
- Check if MySQL is running
- Verify credentials in `application.properties`
- Ensure database `patrakosh_db` exists

### Issue: "Module not found" error
**Solution**: 
- Make sure Java 17+ is installed
- Run `java -version` to verify
- Use Maven to run: `mvn javafx:run`

### Issue: Files not uploading
**Solution**: 
- Check if `storage/` directory exists (it's auto-created)
- Verify you have write permissions
- Check available disk space

## 📱 Features at a Glance

| Feature | How to Use |
|---------|-----------|
| Upload File | Click "⬆ Upload File" button |
| Download File | Click ⬇ button next to file |
| Delete File | Click 🗑 button next to file |
| Search Files | Type in search bar |
| Refresh List | Click 🔄 button |
| Logout | Click 👤 menu → Logout |

## 🎨 UI Color Scheme

- **Primary Blue**: #2196F3
- **Success Green**: #4CAF50
- **Error Red**: #F44336
- **Background**: #F5F5F5
- **Cards**: #FFFFFF

## 📊 Default Test Account (Optional)

You can manually insert a test account in MySQL:

```sql
USE patrakosh_db;

-- Password is "test123" (SHA-256 hashed)
INSERT INTO users (username, email, password) VALUES 
('testuser', 'test@example.com', 'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae');
```

Login with:
- Username: `testuser`
- Password: `test123`

## 🚀 Running from IDE

### IntelliJ IDEA
1. Open project
2. Wait for Maven to download dependencies
3. Right-click `Launcher.java`
4. Select "Run 'Launcher.main()'"

### Eclipse
1. Import as Maven project
2. Update Maven dependencies
3. Right-click `Launcher.java`
4. Run As → Java Application

### VS Code
1. Open project folder
2. Install Java Extension Pack
3. Open `Launcher.java`
4. Click "Run" above main method

## 📦 File Storage Location

Files are stored in: `storage/user_[USER_ID]/`

Example: User with ID 1 → `storage/user_1/`

## 🔐 Security Notes

- Passwords are hashed with SHA-256
- Never share your `application.properties` with passwords
- Database uses prepared statements (SQL injection safe)

## 💡 Tips

1. **Search is instant** - Just start typing
2. **File icons** - Different icons for different file types
3. **Time format** - Shows "2 hours ago", "3 days ago", etc.
4. **Size format** - Auto-converts to KB, MB, GB
5. **Duplicate files** - Auto-renamed with _1, _2, etc.

## 📞 Need Help?

Check the full README.md for detailed documentation.

---

**Happy File Storing! 📦**
