@echo off
title PatraKosh - One-Click Installer and Launcher
color 0A
setlocal enabledelayedexpansion

cd /d "%~dp0"

cls
echo.
echo ==========================================
echo    PatraKosh - One-Click Setup
echo ==========================================
echo.
echo This will automatically:
echo  - Install Java 21 (if needed)
echo  - Install Maven 3.9 (if needed)
echo  - Install MySQL 8.0 (if needed)
echo  - Setup database
echo  - Launch PatraKosh
echo.
echo First-time setup may take 15-20 minutes.
echo Please be patient!
echo.
pause

:: Create tools directory
if not exist "tools" mkdir "tools"
if not exist "%TEMP%\PatraKosh-Setup" mkdir "%TEMP%\PatraKosh-Setup"

:: ============================================
:: STEP 1: Install Java
:: ============================================
echo.
echo ==========================================
echo [1/5] Setting up Java 21...
echo ==========================================
echo.

where java >nul 2>&1
if not errorlevel 1 (
    echo [OK] Java already installed
    goto setup_maven
)

:: Check if already installed locally
if exist "tools\java\bin\java.exe" (
    set "PATH=%CD%\tools\java\bin;!PATH!"
    set "JAVA_HOME=%CD%\tools\java"
    echo [OK] Using local Java installation
    goto setup_maven
)

echo Downloading Java 21 (~200MB)...
powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_windows_hotspot_21.0.1_12.zip' -OutFile '%TEMP%\PatraKosh-Setup\java.zip'"

if exist "%TEMP%\PatraKosh-Setup\java.zip" (
    echo Extracting Java...
    powershell -Command "Expand-Archive -Path '%TEMP%\PatraKosh-Setup\java.zip' -DestinationPath 'tools\' -Force"
    
    :: Find the extracted folder
    for /d %%i in ("tools\jdk-21*") do (
        ren "%%i" "java"
        set "PATH=%CD%\tools\java\bin;!PATH!"
        set "JAVA_HOME=%CD%\tools\java"
        echo [OK] Java 21 installed locally
        goto setup_maven
    )
)

echo [ERROR] Java installation failed
echo Please install Java manually from: https://adoptium.net/
pause
exit /b 1

:: ============================================
:: STEP 2: Install Maven
:: ============================================
:setup_maven
echo.
echo ==========================================
echo [2/5] Setting up Maven 3.9...
echo ==========================================
echo.

where mvn >nul 2>&1
if not errorlevel 1 (
    echo [OK] Maven already installed
    goto setup_mysql
)

:: Check if already installed locally
if exist "tools\maven\bin\mvn.cmd" (
    set "PATH=%CD%\tools\maven\bin;!PATH!"
    set "MAVEN_HOME=%CD%\tools\maven"
    echo [OK] Using local Maven installation
    goto setup_mysql
)

echo Downloading Maven 3.9.6 (~10MB)...
powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\PatraKosh-Setup\maven.zip'"

if exist "%TEMP%\PatraKosh-Setup\maven.zip" (
    echo Extracting Maven...
    powershell -Command "Expand-Archive -Path '%TEMP%\PatraKosh-Setup\maven.zip' -DestinationPath 'tools\' -Force"
    
    if exist "tools\apache-maven-3.9.6" (
        ren "tools\apache-maven-3.9.6" "maven"
        set "PATH=%CD%\tools\maven\bin;!PATH!"
        set "MAVEN_HOME=%CD%\tools\maven"
        echo [OK] Maven 3.9.6 installed locally
        goto setup_mysql
    )
)

echo [ERROR] Maven installation failed
echo Please install Maven manually from: https://maven.apache.org/download.cgi
pause
exit /b 1

:: ============================================
:: STEP 3: Install MySQL
:: ============================================
:setup_mysql
echo.
echo ==========================================
echo [3/5] Setting up MySQL 8.0...
echo ==========================================
echo.

:: Check for MySQL
where mysql >nul 2>&1
if not errorlevel 1 (
    echo [OK] MySQL already installed
    goto setup_database
)

if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin;!PATH!"
    echo [OK] MySQL Server 8.0 found
    goto setup_database
)

if exist "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" (
    set "PATH=C:\Program Files\MySQL\MySQL Server 9.0\bin;!PATH!"
    echo [OK] MySQL Server 9.0 found
    goto setup_database
)

echo MySQL not found - installing...
echo Downloading MySQL Installer (~2MB)...
powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.40.0.msi' -OutFile '%TEMP%\PatraKosh-Setup\mysql-installer.msi'"

if exist "%TEMP%\PatraKosh-Setup\mysql-installer.msi" (
    echo.
    echo Installing MySQL Server...
    echo IMPORTANT: When the installer opens:
    echo  1. Choose "Server Only"
    echo  2. Set a root password (remember it!)
    echo  3. Keep default settings
    echo.
    echo Press any key to start MySQL installation...
    pause >nul
    
    start /wait msiexec /i "%TEMP%\PatraKosh-Setup\mysql-installer.msi" /qb
    
    timeout /t 5 >nul
    net start MySQL80 >nul 2>&1
    
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin;!PATH!"
        echo [OK] MySQL installed successfully
        goto setup_database
    )
)

echo [WARNING] MySQL installation incomplete
echo You may need to install MySQL manually
echo Continuing anyway...

:: ============================================
:: STEP 4: Setup Database
:: ============================================
:setup_database
echo.
echo ==========================================
echo [4/5] Creating database...
echo ==========================================
echo.

set /p MYSQL_PASSWORD="Enter your MySQL root password (or press Enter if none): "

if "%MYSQL_PASSWORD%"=="" (
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;" 2>nul
    if not errorlevel 1 (
        echo [OK] Database created
        mysql -u root patrakosh_db < database_setup.sql 2>nul
        echo [OK] Tables created
        
        :: Update config
        powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=' | Set-Content 'src\main\resources\application.properties'"
        goto build_app
    )
)

mysql -u root -p%MYSQL_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;" 2>nul
if not errorlevel 1 (
    echo [OK] Database created
    mysql -u root -p%MYSQL_PASSWORD% patrakosh_db < database_setup.sql 2>nul
    echo [OK] Tables created
    
    :: Update config
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=%MYSQL_PASSWORD%' | Set-Content 'src\main\resources\application.properties'"
    goto build_app
)

echo [WARNING] Database setup had issues
echo You may need to run setup-database.bat manually
echo Continuing anyway...

:: ============================================
:: STEP 5: Build and Launch
:: ============================================
:build_app
echo.
echo ==========================================
echo [5/5] Building and launching PatraKosh...
echo ==========================================
echo.

if not exist "storage" mkdir "storage"

echo This may take 5-10 minutes on first run...
echo Downloading dependencies and compiling...
echo.

call mvn clean javafx:run

echo.
echo ==========================================
echo    PatraKosh Session Complete
echo ==========================================
echo.
echo Next time, just run this script again!
echo All dependencies are now installed.
echo.
pause
exit /b 0
