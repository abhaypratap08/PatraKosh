@echo off
title PatraKosh - Database Setup and Configuration
color 0B
setlocal enabledelayedexpansion

echo.
echo ==========================================
echo    PatraKosh Database Setup
echo ==========================================
echo.
echo This will:
echo 1. Find MySQL installation
echo 2. Create the database
echo 3. Configure application.properties
echo.
pause

:: Step 1: Find MySQL
echo.
echo [1/3] Locating MySQL installation...
echo.

set "MYSQL_PATH="
set "MYSQL_VERSION="

:: Check for MySQL 9.0
if exist "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 9.0\bin"
    set "MYSQL_VERSION=9.0"
    echo [OK] Found MySQL Server 9.0
    goto mysql_found
)

:: Check for MySQL 8.0
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin"
    set "MYSQL_VERSION=8.0"
    echo [OK] Found MySQL Server 8.0
    goto mysql_found
)

:: Check if mysql is in PATH
where mysql >nul 2>&1
if not errorlevel 1 (
    for /f "tokens=*" %%i in ('where mysql') do (
        set "MYSQL_PATH=%%~dpi"
        set "MYSQL_VERSION=Unknown"
        echo [OK] Found MySQL in PATH
        goto mysql_found
    )
)

:: MySQL not found - install automatically
echo [WARNING] MySQL not found!
echo.
echo MySQL is required for PatraKosh to work.
echo Installing MySQL 8.0 Community Server automatically...
echo This will download ~400MB and may take 10-15 minutes.
echo.
pause

:install_mysql
echo.
echo ==========================================
echo    Installing MySQL Server 8.0
echo ==========================================
echo.
echo This may take 10-15 minutes...
echo Please be patient!
echo.

:: Create temp directory
if not exist "%TEMP%\PatraKosh-MySQL" mkdir "%TEMP%\PatraKosh-MySQL"

:: Download MySQL Installer
echo [1/4] Downloading MySQL Installer (~2MB)...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.40.0.msi' -OutFile '%TEMP%\PatraKosh-MySQL\mysql-installer.msi' -UseBasicParsing } catch { Write-Host 'Download failed. Trying alternate link...'; Invoke-WebRequest -Uri 'https://cdn.mysql.com/Downloads/MySQLInstaller/mysql-installer-community-8.0.40.0.msi' -OutFile '%TEMP%\PatraKosh-MySQL\mysql-installer.msi' -UseBasicParsing }}"

if not exist "%TEMP%\PatraKosh-MySQL\mysql-installer.msi" (
    echo [ERROR] Failed to download MySQL installer
    echo Please download manually from: https://dev.mysql.com/downloads/mysql/
    pause
    exit /b 1
)

echo [OK] MySQL Installer downloaded

:: Install MySQL
echo.
echo [2/4] Installing MySQL Server...
echo.
echo IMPORTANT: During installation:
echo 1. Choose "Server Only" installation type
echo 2. Set a root password (remember it!)
echo 3. Keep default settings for everything else
echo.
echo Press any key to start MySQL installation...
pause >nul

start /wait msiexec /i "%TEMP%\PatraKosh-MySQL\mysql-installer.msi" /qb

echo.
echo [3/4] Configuring MySQL...
echo.

:: Wait for MySQL service to be available
timeout /t 5 >nul

:: Try to start MySQL service
net start MySQL80 >nul 2>&1
if errorlevel 1 (
    net start MySQL >nul 2>&1
)

:: Add MySQL to PATH
echo [4/4] Adding MySQL to system PATH...

if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin"
    setx PATH "%PATH%;%MYSQL_PATH%" /M >nul 2>&1
    set "PATH=%PATH%;%MYSQL_PATH%"
    echo [OK] MySQL added to PATH
) else (
    echo [WARNING] MySQL installation path not found
    echo You may need to add MySQL to PATH manually
)

:: Clean up
rmdir /s /q "%TEMP%\PatraKosh-MySQL" >nul 2>&1

echo.
echo ==========================================
echo    MySQL Installation Complete!
echo ==========================================
echo.
echo MySQL Server 8.0 has been installed.
echo.
echo Continuing with database setup...
echo.
timeout /t 3 >nul

:: Set MySQL path and continue
set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin"
set "MYSQL_VERSION=8.0"
goto mysql_found

:mysql_found
echo MySQL Location: %MYSQL_PATH%
echo.

:: Step 2: Get MySQL password
echo [2/3] MySQL Configuration
echo.
set /p MYSQL_PASSWORD="Enter your MySQL root password (or press Enter if no password): "

:: Test MySQL connection
echo.
echo Testing MySQL connection...
if "%MYSQL_PASSWORD%"=="" (
    "%MYSQL_PATH%\mysql.exe" -u root -e "SELECT 1;" >nul 2>&1
) else (
    "%MYSQL_PATH%\mysql.exe" -u root -p%MYSQL_PASSWORD% -e "SELECT 1;" >nul 2>&1
)

if errorlevel 1 (
    echo [ERROR] Cannot connect to MySQL!
    echo.
    echo Troubleshooting:
    echo 1. Make sure MySQL service is running: net start mysql
    echo 2. Check your password is correct
    echo 3. Try running as Administrator
    echo.
    pause
    exit /b 1
)

echo [OK] MySQL connection successful!

:: Step 3: Create database
echo.
echo [3/3] Creating database and tables...
echo.

if "%MYSQL_PASSWORD%"=="" (
    "%MYSQL_PATH%\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;"
    if not errorlevel 1 (
        echo [OK] Database 'patrakosh_db' created
        "%MYSQL_PATH%\mysql.exe" -u root patrakosh_db < database_setup.sql
        if not errorlevel 1 (
            echo [OK] Tables created successfully
            goto configure_app
        )
    )
) else (
    "%MYSQL_PATH%\mysql.exe" -u root -p%MYSQL_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;"
    if not errorlevel 1 (
        echo [OK] Database 'patrakosh_db' created
        "%MYSQL_PATH%\mysql.exe" -u root -p%MYSQL_PASSWORD% patrakosh_db < database_setup.sql
        if not errorlevel 1 (
            echo [OK] Tables created successfully
            goto configure_app
        )
    )
)

echo [ERROR] Failed to create database
pause
exit /b 1

:configure_app
echo.
echo [4/4] Configuring application.properties...
echo.

:: Update application.properties with the password
if exist "src\main\resources\application.properties" (
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=%MYSQL_PASSWORD%' | Set-Content 'src\main\resources\application.properties'"
    echo [OK] application.properties updated
) else (
    echo [WARNING] application.properties not found
)

:: Verify database setup
echo.
echo Verifying database setup...
if "%MYSQL_PASSWORD%"=="" (
    "%MYSQL_PATH%\mysql.exe" -u root -e "USE patrakosh_db; SHOW TABLES;"
) else (
    "%MYSQL_PATH%\mysql.exe" -u root -p%MYSQL_PASSWORD% -e "USE patrakosh_db; SHOW TABLES;"
)

echo.
echo ==========================================
echo    SETUP COMPLETE!
echo ==========================================
echo.
echo Database: patrakosh_db
echo Tables: users, files
echo Status: Ready to use
echo.
echo You can now run PatraKosh using runApp.bat
echo.
pause
exit /b 0
