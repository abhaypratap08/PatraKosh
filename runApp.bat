@echo off
title PatraKosh - File Storage Application Launcher
color 0A
setlocal enabledelayedexpansion

cd /d "%~dp0"

:main_menu
cls
echo.
echo ==========================================
echo    PatraKosh - File Storage Application
echo    TeamAlgoNauts Project - v1.0.0
echo ==========================================
echo.
echo Choose an option:
echo.
echo 1. START APPLICATION (Recommended)
echo 2. VERIFY SYSTEM SETUP
echo 3. MANUAL DATABASE SETUP GUIDE
echo 4. EXIT
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto start_app
if "%choice%"=="2" goto verify_setup
if "%choice%"=="3" goto database_guide
if "%choice%"=="4" goto exit_app
echo Invalid choice. Please try again.
timeout /t 2 >nul
goto main_menu

:start_app
cls
echo.
echo ==========================================
echo    STARTING PATRAKOSH APPLICATION
echo ==========================================
echo.

:: Quick checks
echo Checking prerequisites...
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found!
    echo Please install Java 17+ from: https://adoptium.net/
    pause
    goto main_menu
)
echo [OK] Java found

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven not found!
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    goto main_menu
)
echo [OK] Maven found

if not exist "pom.xml" (
    echo [ERROR] pom.xml not found!
    pause
    goto main_menu
)
echo [OK] Project files found

if not exist "storage" mkdir "storage"

echo.
echo ==========================================
echo    Building and Starting PatraKosh...
echo    This may take 5-10 minutes first time
echo    JavaFX window will open shortly...
echo ==========================================
echo.

:: Run Maven with JavaFX
mvn clean javafx:run

echo.
echo ==========================================
echo    PatraKosh Session Ended
echo ==========================================
echo.
set /p restart="Return to menu? (y/n): "
if /i "%restart%"=="y" goto main_menu
goto exit_app

:database_guide
cls
echo.
echo ==========================================
echo    DATABASE SETUP GUIDE
echo ==========================================
echo.
echo Manual Database Setup Steps:
echo.
echo 1. Install MySQL Server
echo    Download: https://dev.mysql.com/downloads/mysql/
echo.
echo 2. Start MySQL Service
echo    Run: net start mysql
echo.
echo 3. Create Database
echo    Run: mysql -u root -p ^< database_setup.sql
echo.
echo 4. Configure Application
echo    Edit: src\main\resources\application.properties
echo    Replace YOUR_MYSQL_PASSWORD_HERE with your password
echo.
pause
goto main_menu

:verify_setup
cls
echo.
echo ==========================================
echo    System Verification
echo ==========================================
echo.

echo Checking Java...
where java >nul 2>&1
if errorlevel 1 (
    echo [!] Java NOT found
    echo Install from: https://adoptium.net/
) else (
    echo [OK] Java is installed
)

echo.
echo Checking Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo [!] Maven NOT found
    echo Install from: https://maven.apache.org/download.cgi
) else (
    echo [OK] Maven is installed
)

echo.
echo Checking Project Files...
if not exist "pom.xml" (
    echo [!] pom.xml NOT found
) else (
    echo [OK] pom.xml found
)

if not exist "src\main\java" (
    echo [!] Source code NOT found
) else (
    echo [OK] Source code found
)

echo.
echo Checking MySQL...
where mysql >nul 2>&1
if errorlevel 1 (
    echo [!] MySQL NOT found in PATH
    echo Install from: https://dev.mysql.com/downloads/mysql/
) else (
    echo [OK] MySQL found
)

echo.
echo Checking Database Config...
if exist "src\main\resources\application.properties" (
    findstr /C:"YOUR_MYSQL_PASSWORD_HERE" "src\main\resources\application.properties" >nul 2>&1
    if not errorlevel 1 (
        echo [!] Database password NOT configured
        echo Edit: src\main\resources\application.properties
    ) else (
        echo [OK] Database config looks good
    )
) else (
    echo [!] application.properties NOT found
)

echo.
echo ==========================================
echo    Verification Complete
echo ==========================================
echo.
pause
goto main_menu

:exit_app
cls
echo.
echo ==========================================
echo    Thank you for using PatraKosh!
echo ==========================================
echo.
echo Your files are stored in: storage\
echo Run this script anytime to access PatraKosh
echo.
echo Built by TeamAlgoNauts
echo.
pause
exit /b 0
