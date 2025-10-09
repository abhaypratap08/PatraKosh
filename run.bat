@echo off
setlocal enabledelayedexpansion
title PatraKosh - File Storage Application
color 0A

echo.
echo ==========================================
echo    🚀 PatraKosh - File Storage App 🚀
echo    TeamAlgoNauts Project - v1.0.0
echo ==========================================
echo.

REM Change to script directory
cd /d "%~dp0"

REM Check Java
echo [1/6] Checking Java installation...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo ❌ ERROR: Java not found!
    echo.
    echo Please install Java 17+ and add to PATH
    echo Download: https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)
echo ✅ Java is ready

REM Check Maven
echo [2/6] Checking Maven installation...
mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo ❌ ERROR: Maven not found!
    echo.
    echo Please install Maven 3.6+ and add to PATH
    echo Download: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)
echo ✅ Maven is ready

REM Check project files
echo [3/6] Validating project structure...
if not exist "pom.xml" (
    echo ❌ ERROR: pom.xml not found!
    echo Make sure you're running this from the project root directory.
    pause
    exit /b 1
)
if not exist "src\main\java" (
    echo ❌ ERROR: Source code not found!
    echo Project structure is incomplete.
    pause
    exit /b 1
)
echo ✅ Project structure is valid

REM Check MySQL connection
echo [4/6] Checking database setup...
echo.
echo 📋 Database Requirements Checklist:
echo   1. MySQL Server is running
echo   2. Database 'patrakosh_db' exists
echo   3. Password updated in application.properties
echo.
echo 💡 Quick Setup Commands:
echo   mysql -u root -p ^< database_setup.sql
echo   Edit: src\main\resources\application.properties
echo.

set /p dbready="Is your database ready? (y/n/skip): "
if /i "!dbready!"=="n" (
    echo.
    echo 🔧 Database Setup Instructions:
    echo 1. Start MySQL service
    echo 2. Open MySQL command line or Workbench
    echo 3. Run: mysql -u root -p ^< database_setup.sql
    echo 4. Edit src\main\resources\application.properties
    echo 5. Change db.password to your MySQL password
    echo.
    pause
    exit /b 0
)
if /i "!dbready!"=="skip" (
    echo ⚠️  Skipping database check - app may fail to start
)
echo ✅ Database setup confirmed

REM Build project
echo [5/6] Building PatraKosh...
echo 📦 Downloading dependencies and compiling...
echo (This may take a few minutes on first run)
echo.

mvn clean compile -q
if !errorlevel! neq 0 (
    echo.
    echo ❌ BUILD FAILED!
    echo.
    echo 🔍 Common Solutions:
    echo   - Check internet connection (for dependencies)
    echo   - Ensure Java 17+ is installed
    echo   - Try running: mvn clean install
    echo.
    pause
    exit /b 1
)
echo ✅ Build successful!

REM Run application
echo [6/6] Starting PatraKosh Application...
echo.
echo 🎯 Launching PatraKosh...
echo 📱 The application window will open shortly
echo 🔄 Please wait while JavaFX initializes...
echo.

mvn javafx:run

REM Handle exit
echo.
if !errorlevel! equ 0 (
    echo ✅ PatraKosh closed successfully
) else (
    echo ⚠️  Application exited with errors
    echo.
    echo 🔍 Troubleshooting:
    echo   - Check database connection
    echo   - Verify MySQL is running
    echo   - Check application.properties
)

echo.
echo ==========================================
echo    Thank you for using PatraKosh! 🙏
echo    TeamAlgoNauts - File Storage Solution
echo ==========================================
echo.
pause
