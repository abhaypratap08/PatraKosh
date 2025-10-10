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

:: Check Java
echo [1/3] Checking Java...
where java >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Java not found!
    echo.
    set /p install_java="Install Java 21 automatically? (y/n): "
    if /i "!install_java!"=="y" goto install_java
    if /i "!install_java!"=="yes" goto install_java
    echo.
    echo Please install Java 17+ from: https://adoptium.net/
    pause
    goto main_menu
)
echo [OK] Java found

:check_maven
echo [2/3] Checking Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Maven not found!
    echo.
    set /p install_maven="Install Maven 3.9 automatically? (y/n): "
    if /i "!install_maven!"=="y" goto install_maven
    if /i "!install_maven!"=="yes" goto install_maven
    echo.
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    goto main_menu
)
echo [OK] Maven found
goto check_project

:install_java
echo.
echo ==========================================
echo    Installing Java 21 (Eclipse Temurin)
echo ==========================================
echo.
echo Downloading Java 21 (~200MB)...
echo This may take 5-10 minutes...
echo.

if not exist "%TEMP%\PatraKosh-Setup" mkdir "%TEMP%\PatraKosh-Setup"

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Write-Host 'Downloading Java 21...'; Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_windows_hotspot_21.0.1_12.msi' -OutFile '%TEMP%\PatraKosh-Setup\java21.msi' -UseBasicParsing } catch { Write-Host 'Download failed: ' $_.Exception.Message; exit 1 }}"

if not exist "%TEMP%\PatraKosh-Setup\java21.msi" (
    echo [ERROR] Failed to download Java
    echo Please install manually from: https://adoptium.net/
    pause
    goto main_menu
)

echo [OK] Java downloaded
echo.
echo Installing Java 21...
echo Please wait, this may take a few minutes...
start /wait msiexec /i "%TEMP%\PatraKosh-Setup\java21.msi" /quiet /norestart ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome

:: Clean up
del "%TEMP%\PatraKosh-Setup\java21.msi" >nul 2>&1

:: Refresh environment
echo.
echo [OK] Java 21 installed!
echo.
echo IMPORTANT: Please restart this script for changes to take effect.
echo.
pause
exit /b 0

:install_maven
echo.
echo ==========================================
echo    Installing Maven 3.9.6
echo ==========================================
echo.
echo Downloading Maven (~10MB)...
echo.

if not exist "%TEMP%\PatraKosh-Setup" mkdir "%TEMP%\PatraKosh-Setup"

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Write-Host 'Downloading Maven...'; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\PatraKosh-Setup\maven.zip' -UseBasicParsing } catch { Write-Host 'Download failed: ' $_.Exception.Message; exit 1 }}"

if not exist "%TEMP%\PatraKosh-Setup\maven.zip" (
    echo [ERROR] Failed to download Maven
    echo Please install manually from: https://maven.apache.org/download.cgi
    pause
    goto main_menu
)

echo [OK] Maven downloaded
echo.
echo Installing Maven...

:: Extract Maven
powershell -Command "Expand-Archive -Path '%TEMP%\PatraKosh-Setup\maven.zip' -DestinationPath 'C:\Tools\' -Force"

if exist "C:\Tools\apache-maven-3.9.6" (
    :: Add to PATH
    setx PATH "%PATH%;C:\Tools\apache-maven-3.9.6\bin" /M >nul 2>&1
    setx MAVEN_HOME "C:\Tools\apache-maven-3.9.6" /M >nul 2>&1
    
    :: Update current session
    set "PATH=%PATH%;C:\Tools\apache-maven-3.9.6\bin"
    set "MAVEN_HOME=C:\Tools\apache-maven-3.9.6"
    
    echo [OK] Maven installed to C:\Tools\apache-maven-3.9.6
) else (
    echo [ERROR] Maven installation failed
    pause
    goto main_menu
)

:: Clean up
del "%TEMP%\PatraKosh-Setup\maven.zip" >nul 2>&1

echo.
echo [OK] Maven 3.9.6 installed!
echo.
echo IMPORTANT: Please restart this script for changes to take effect.
echo.
pause
exit /b 0

:check_project
echo [3/3] Checking project files...

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
