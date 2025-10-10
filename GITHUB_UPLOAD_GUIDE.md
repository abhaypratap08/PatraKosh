# GitHub Upload Guide for PatraKosh

## 🔐 IMPORTANT: Security Before Upload

### Files to Check Before Uploading:

#### 1. **application.properties** ✅ FIXED
**Location:** `src/main/resources/application.properties`

**Current Status:** ✅ Password is now a placeholder
```properties
db.password=YOUR_MYSQL_PASSWORD_HERE
```

**What users need to do:**
- Replace `YOUR_MYSQL_PASSWORD_HERE` with their actual MySQL password
- This is documented in README.md

#### 2. **.gitignore** ✅ Already Configured
The `.gitignore` file already excludes:
- `target/` (build files)
- `storage/` (user data)
- `dependency-reduced-pom.xml` (generated file)
- IDE files

---

## 📤 GitHub Upload Sequence

### Method 1: Using Git Command Line (Recommended)

```bash
# 1. Initialize Git (if not already done)
git init

# 2. Add all files
git add .

# 3. Commit with message
git commit -m "Initial commit: Complete PatraKosh file storage application"

# 4. Add your GitHub repository as remote
git remote add origin https://github.com/abhaypratap08/PatraKosh.git

# 5. Push to GitHub
git push -u origin main
```

### Method 2: Using GitHub Desktop (Easiest)

1. Open GitHub Desktop
2. Click "Add" → "Add Existing Repository"
3. Select your PatraKosh folder
4. Click "Publish repository"
5. Choose "abhaypratap08/PatraKosh"
6. Click "Publish"

### Method 3: Using GitHub Web Interface

Upload files in this order:

#### Phase 1: Essential Files (Upload First)
1. **README.md** ⭐
2. **.gitignore**
3. **pom.xml**

#### Phase 2: Documentation
4. **QUICKSTART.md**
5. **GETTING_STARTED.md**
6. **PROJECT_SUMMARY.md**
7. **ARCHITECTURE.md**
8. **SETUP_CHECKLIST.md**

#### Phase 3: Database & Scripts
9. **database_setup.sql**
10. **runApp.bat**

#### Phase 4: Source Code
11. **src/** (entire folder with all subfolders)
    - Upload the complete `src` directory structure

---

## ⚠️ Files to EXCLUDE from GitHub

**DO NOT UPLOAD:**
- ❌ `target/` folder (build artifacts)
- ❌ `storage/` folder (user data)
- ❌ `dependency-reduced-pom.xml` (generated file)
- ❌ `.idea/` folder (IDE settings)
- ❌ `*.iml` files (IDE files)
- ❌ `logs/` folder (if exists)

**Your `.gitignore` already handles these!**

---

## 🔧 Local Development vs GitHub

### For Local Development:
```properties
# src/main/resources/application.properties
db.password=0101  # Your actual password
```

### For GitHub:
```properties
# src/main/resources/application.properties
db.password=YOUR_MYSQL_PASSWORD_HERE  # Placeholder
```

**Current Status:** ✅ Set to placeholder (safe for GitHub)

---

## 📝 Commit Message Suggestions

### If uploading all at once:
```
Initial commit: Complete PatraKosh file storage application

- JavaFX desktop application with MySQL backend
- User authentication and file management
- Modern UI with Google Pixel-inspired design
- Comprehensive documentation and setup scripts
- Auto-setup batch script for Windows
```

### If uploading in phases:
```
Phase 1: Add project documentation and configuration
Phase 2: Add database schema and setup scripts
Phase 3: Add source code and application logic
Phase 4: Add UI layouts and resources
```

---

## 🎯 After Upload Checklist

### On GitHub:
- [ ] Add repository description: "Modern desktop file storage application built with JavaFX and MySQL"
- [ ] Add topics: `javafx`, `mysql`, `file-storage`, `desktop-app`, `java`, `maven`
- [ ] Make repository public (if desired)
- [ ] Create a release (tag as v1.0.0)
- [ ] Add screenshots to README (optional)

### Test the Repository:
- [ ] Clone to a different folder
- [ ] Follow QUICKSTART.md instructions
- [ ] Verify application runs correctly
- [ ] Check all documentation links work

---

## 🚀 Quick Upload Commands

```bash
# One-time setup
git init
git add .
git commit -m "Initial commit: Complete PatraKosh application"
git branch -M main
git remote add origin https://github.com/abhaypratap08/PatraKosh.git
git push -u origin main

# Future updates
git add .
git commit -m "Update: [describe your changes]"
git push
```

---

## 🔐 Security Best Practices

### ✅ DO:
- Use placeholder passwords in GitHub
- Document setup steps clearly
- Include .gitignore file
- Keep sensitive data local

### ❌ DON'T:
- Upload actual passwords
- Upload user data (storage/ folder)
- Upload build artifacts (target/ folder)
- Upload IDE-specific files

---

## 📞 Need Help?

If you encounter issues:
1. Check that `.gitignore` is working
2. Verify no sensitive data in files
3. Test clone in a new folder
4. Review GitHub's upload limits (100MB per file)

---

## ✅ Pre-Upload Verification

Run this checklist before uploading:

```bash
# Check for sensitive data
findstr /S /I "password" src\main\resources\application.properties
# Should show: db.password=YOUR_MYSQL_PASSWORD_HERE

# Verify .gitignore exists
dir .gitignore

# Check no build artifacts
dir target
# Should not exist or be empty

# Verify project structure
dir /S src
```

---

**Status:** ✅ Ready for GitHub Upload!

Your `application.properties` is now safe with placeholder password.
Users will need to configure their own MySQL password after cloning.

**Repository:** https://github.com/abhaypratap08/PatraKosh
