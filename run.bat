@echo off
setlocal enabledelayedexpansion

:: PatraKosh Auto-Setup and Run Script
:: This script automatically installs Java and Maven if needed, then runs the application

echo.
echo ========================================
echo    PatraKosh - Auto Setup and Run
echo ========================================
echo.
echo Checking system requirements...
echo.

:: Color codes for better output
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "RESET=[0m"

:: Variables
set "JAVA_REQUIRED=17"
set "MAVEN_REQUIRED=3.6"
set "JAVA_INSTALLED=false"
set "MAVEN_INSTALLED=false"
set "NEED_RESTART=false"

:: Check if Java is installed
echo %BLUE%Checking Java installation...%RESET%
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✓ Java is installed%RESET%
    set "JAVA_INSTALLED=true"
    
    :: Check Java version
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set "JAVA_VERSION=%%g"
        set "JAVA_VERSION=!JAVA_VERSION:"=!"
    )
    echo   Version: !JAVA_VERSION!
) else (
    echo %RED%✗ Java not found%RESET%
    set "JAVA_INSTALLED=false"
)

echo.

:: Check if Maven is installed
echo %BLUE%Checking Maven installation...%RESET%
mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✓ Maven is installed%RESET%
    set "MAVEN_INSTALLED=true"
    
    :: Get Maven version
    for /f "tokens=3" %%g in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do (
        set "MAVEN_VERSION=%%g"
    )
    echo   Version: !MAVEN_VERSION!
) else (
    echo %RED%✗ Maven not found%RESET%
    set "MAVEN_INSTALLED=false"
)

echo.

:: Install Java if needed
if "!JAVA_INSTALLED!"=="false" (
    echo %YELLOW%Installing Java 21...%RESET%
    echo.
    
    :: Create temp directory
    if not exist "%TEMP%\PatraKosh-Setup" mkdir "%TEMP%\PatraKosh-Setup"
    
    echo Downloading Java 21 (Amazon Corretto)...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.msi' -OutFile '%TEMP%\PatraKosh-Setup\java21.msi'}"
    
    if exist "%TEMP%\PatraKosh-Setup\java21.msi" (
        echo Installing Java 21...
        msiexec /i "%TEMP%\PatraKosh-Setup\java21.msi" /quiet /norestart
        
        :: Set JAVA_HOME
        for /f "tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /s /v JavaHome 2^>nul ^| findstr "JavaHome"') do (
            set "NEW_JAVA_HOME=%%b"
        )
        
        if defined NEW_JAVA_HOME (
            setx JAVA_HOME "!NEW_JAVA_HOME!" /M >nul 2>&1
            setx PATH "%PATH%;!NEW_JAVA_HOME!\bin" /M >nul 2>&1
            set "NEED_RESTART=true"
            echo %GREEN%✓ Java 21 installed successfully%RESET%
        ) else (
            echo %RED%✗ Java installation may have failed%RESET%
        )
    ) else (
        echo %RED%✗ Failed to download Java installer%RESET%
        echo Please install Java 17+ manually from: https://adoptium.net/
        pause
        exit /b 1
    )
    echo.
)

:: Install Maven if needed
if "!MAVEN_INSTALLED!"=="false" (
    echo %YELLOW%Installing Maven 3.9.5...%RESET%
    echo.
    
    :: Create tools directory
    if not exist "C:\Tools" mkdir "C:\Tools"
    
    echo Downloading Maven 3.9.5...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip' -OutFile '%TEMP%\PatraKosh-Setup\maven.zip'}"
    
    if exist "%TEMP%\PatraKosh-Setup\maven.zip" (
        echo Extracting Maven...
        powershell -Command "Expand-Archive -Path '%TEMP%\PatraKosh-Setup\maven.zip' -DestinationPath 'C:\Tools\' -Force"
        
        :: Set Maven environment variables
        setx MAVEN_HOME "C:\Tools\apache-maven-3.9.5" /M >nul 2>&1
        setx PATH "%PATH%;C:\Tools\apache-maven-3.9.5\bin" /M >nul 2>&1
        set "NEED_RESTART=true"
        echo %GREEN%✓ Maven 3.9.5 installed successfully%RESET%
    ) else (
        echo %RED%✗ Failed to download Maven%RESET%
        echo Please install Maven manually from: https://maven.apache.org/download.cgi
        pause
        exit /b 1
    )
    echo.
)

:: Clean up temp files
if exist "%TEMP%\PatraKosh-Setup" (
    rmdir /s /q "%TEMP%\PatraKosh-Setup" >nul 2>&1
)

:: Check if restart is needed
if "!NEED_RESTART!"=="true" (
    echo %YELLOW%⚠ Environment variables have been updated%RESET%
    echo %YELLOW%⚠ Please restart your command prompt or computer%RESET%
    echo %YELLOW%⚠ Then run this script again to start PatraKosh%RESET%
    echo.
    pause
    exit /b 0
)

:: Update current session PATH if installations were done
if "!JAVA_INSTALLED!"=="false" (
    set "PATH=%PATH%;%JAVA_HOME%\bin"
)
if "!MAVEN_INSTALLED!"=="false" (
    set "PATH=%PATH%;C:\Tools\apache-maven-3.9.5\bin"
)

echo %GREEN%✓ All prerequisites are satisfied!%RESET%
echo.

:: Check if database setup is needed
echo %BLUE%Checking database setup...%RESET%
if not exist "src\main\resources\application.properties" (
    echo %RED%✗ Project files not found. Make sure you're in the PatraKosh directory.%RESET%
    pause
    exit /b 1
)

:: Check if database password is configured
findstr /C:"YOUR_MYSQL_PASSWORD_HERE" "src\main\resources\application.properties" >nul
if %errorlevel% equ 0 (
    echo %YELLOW%⚠ Database password not configured%RESET%
    echo.
    echo Please follow these steps:
    echo 1. Install and start MySQL server
    echo 2. Run: mysql -u root -p ^< database_setup.sql
    echo 3. Edit src\main\resources\application.properties
    echo 4. Replace YOUR_MYSQL_PASSWORD_HERE with your MySQL password
    echo.
    echo %BLUE%Press any key when database is configured...%RESET%
    pause >nul
)

:: Build the project
echo %BLUE%Building PatraKosh...%RESET%
echo.
call mvn clean compile
if %errorlevel% neq 0 (
    echo %RED%✗ Build failed%RESET%
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo %GREEN%✓ Build successful!%RESET%
echo.

:: Run the application
echo %BLUE%Starting PatraKosh...%RESET%
echo.
echo ========================================
echo    PatraKosh is starting...
echo    Close this window to stop the app
echo ========================================
echo.

call mvn javafx:run

echo.
echo %BLUE%PatraKosh has been closed.%RESET%
echo Thank you for using PatraKosh!
echo.
pause
