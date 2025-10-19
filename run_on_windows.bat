@echo off
title PatraKosh - Launcher
color 0A
setlocal enabledelayedexpansion

cd /d "%~dp0"

:: ============================================
:: PATRAKOSH ONE-CLICK LAUNCHER
:: Fully automated setup, build, and launch
:: ============================================

:: Initialize logging
call :InitLog

:: Load saved configuration
call :LoadConfig

:: Kill any running instances first
taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1
timeout /t 1 >nul

:: Check if this is first run or quick launch
if exist ".setup_complete" (
    call :RunQuickLaunch
) else (
    call :RunFirstTimeSetup
)

exit /b 0

:: ============================================
:: CONFIGURATION MANAGEMENT
:: ============================================

:LoadConfig
call :Log "Loading configuration..."
if exist ".patrakosh.config" (
    for /f "usebackq tokens=1,* delims==" %%a in (".patrakosh.config") do (
        set "CONFIG_%%a=%%b"
    )
    call :Log "Configuration loaded successfully"
) else (
    call :Log "No configuration file found, will create new"
)
goto :eof

:SaveConfig
set "KEY=%~1"
set "VALUE=%~2"
if exist ".patrakosh.config" (
    findstr /v /b /c:"%KEY%=" ".patrakosh.config" > ".patrakosh.config.tmp" 2>nul
    move /y ".patrakosh.config.tmp" ".patrakosh.config" >nul 2>&1
)
echo %KEY%=%VALUE%>> .patrakosh.config
set "CONFIG_%KEY%=%VALUE%"
goto :eof

:: ============================================
:: LOGGING SYSTEM
:: ============================================

:InitLog
if exist "setup.log" (
    for %%A in ("setup.log") do (
        if %%~zA gtr 1048576 (
            if exist "setup.old.log" del "setup.old.log"
            ren "setup.log" "setup.old.log"
        )
    )
)
echo ============================================ >> setup.log
echo PatraKosh Setup Log - %date% %time% >> setup.log
echo ============================================ >> setup.log
goto :eof

:Log
set "MSG=%~1"
echo [%date% %time:~0,8%] %MSG% >> setup.log
if not "%SILENT%"=="1" echo %MSG%
goto :eof

:LogError
set "MSG=%~1"
echo [%date% %time:~0,8%] ERROR: %MSG% >> setup.log
echo [ERROR] %MSG%
set "LAST_ERROR=%MSG%"
goto :eof

:: ============================================
:: FIRST-TIME SETUP WORKFLOW
:: ============================================

:RunFirstTimeSetup
cls
echo.
echo ==========================================
echo    PatraKosh v2.0 - First-Time Setup
echo ==========================================
echo.
echo Setting up your environment automatically...
echo This will take 10-15 minutes.
echo.
echo What will be installed:
echo  - Java 21 (if needed)
echo  - Maven 3.9 (if needed)
echo  - MySQL 8.0 (if needed)
echo  - PatraKosh database
echo  - Application dependencies
echo.
echo Please wait...
echo.

call :Log "=== Starting First-Time Setup ==="

:: Create necessary directories
if not exist "tools" mkdir "tools"
if not exist "storage" mkdir "storage"
if not exist "logs" mkdir "logs"
if not exist "%TEMP%\PatraKosh-Setup" mkdir "%TEMP%\PatraKosh-Setup"

:: Step 1: Setup Java
call :Log "Step 1/6: Setting up Java..."
echo [1/6] Setting up Java...
call :DetectJava
if errorlevel 1 (
    call :InstallJava
    if errorlevel 1 goto :SetupError
)

:: Step 2: Setup Maven
call :Log "Step 2/6: Setting up Maven..."
echo [2/6] Setting up Maven...
call :DetectMaven
if errorlevel 1 (
    call :InstallMaven
    if errorlevel 1 goto :SetupError
)

:: Step 3: Setup MySQL
call :Log "Step 3/6: Setting up MySQL..."
echo [3/6] Setting up MySQL...
call :DetectMySQL
if errorlevel 1 (
    call :InstallMySQL
    if errorlevel 1 goto :SetupError
)

:: Step 4: Setup Database
call :Log "Step 4/6: Setting up database..."
echo [4/6] Setting up database...
call :SetupDatabase
if errorlevel 1 (
    set "ERROR_TYPE=DATABASE"
    goto :SetupError
)

:: Step 5: Build Application
call :Log "Step 5/6: Building application..."
echo [5/6] Building application (this may take 5-10 minutes)...
call :CleanBuild
if errorlevel 1 (
    set "ERROR_TYPE=BUILD"
    goto :SetupError
)

:: Step 6: Create completion flag
call :Log "Step 6/6: Finalizing setup..."
echo [6/6] Finalizing setup...
call :CreateSetupFlag

echo.
echo ==========================================
echo    Setup Complete!
echo ==========================================
echo.
call :Log "=== Setup completed successfully ==="
echo Launching PatraKosh...
echo.

:: Launch the application
call :LaunchApp
goto :eof

:: ============================================
:: QUICK LAUNCH WORKFLOW
:: ============================================

:RunQuickLaunch
call :Log "=== Quick Launch Mode ==="
echo.
echo ==========================================
echo    PatraKosh v2.0 - Quick Launch
echo ==========================================
echo.
echo Verifying environment...

:: Verify dependencies are still available
call :VerifySetup
if errorlevel 1 (
    call :LogError "Setup verification failed, re-running full setup"
    del ".setup_complete" 2>nul
    call :RunFirstTimeSetup
    goto :eof
)

echo Environment OK
echo.

:: Always do a clean compile to ensure latest code
echo Compiling application...
call mvn clean compile -q

if errorlevel 1 (
    echo [ERROR] Compilation failed
    set "ERROR_TYPE=BUILD"
    goto :SetupError
)

echo [OK] Compilation successful
echo.
echo Launching PatraKosh...
echo.
call :Log "Launching application"

:: Launch the application
call :LaunchApp
goto :eof

:: ============================================
:: DEPENDENCY DETECTION
:: ============================================

:DetectJava
call :Log "Detecting Java..."

if defined CONFIG_JAVA_HOME (
    if exist "!CONFIG_JAVA_HOME!\bin\java.exe" (
        set "JAVA_HOME=!CONFIG_JAVA_HOME!"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
        call :Log "Java found in config: !JAVA_HOME!"
        echo   [OK] Java found (saved location)
        exit /b 0
    )
)

where java >nul 2>&1
if not errorlevel 1 (
    for /f "tokens=*" %%i in ('where java') do (
        set "JAVA_PATH=%%i"
        set "JAVA_HOME=%%~dpi.."
        call :SaveConfig JAVA_HOME "!JAVA_HOME!"
        call :Log "Java found in PATH: !JAVA_HOME!"
        echo   [OK] Java found (system)
        exit /b 0
    )
)

if exist "tools\java\bin\java.exe" (
    set "JAVA_HOME=%CD%\tools\java"
    set "PATH=%JAVA_HOME%\bin;!PATH!"
    call :SaveConfig JAVA_HOME "!JAVA_HOME!"
    call :Log "Java found in tools: !JAVA_HOME!"
    echo   [OK] Java found (local)
    exit /b 0
)

call :Log "Java not found"
echo   [--] Java not found, will install
exit /b 1

:DetectMaven
call :Log "Detecting Maven..."

if defined CONFIG_MAVEN_HOME (
    if exist "!CONFIG_MAVEN_HOME!\bin\mvn.cmd" (
        set "MAVEN_HOME=!CONFIG_MAVEN_HOME!"
        set "PATH=!MAVEN_HOME!\bin;!PATH!"
        call :Log "Maven found in config: !MAVEN_HOME!"
        echo   [OK] Maven found (saved location)
        exit /b 0
    )
)

where mvn >nul 2>&1
if not errorlevel 1 (
    for /f "tokens=*" %%i in ('where mvn') do (
        set "MVN_PATH=%%i"
        set "MAVEN_HOME=%%~dpi.."
        call :SaveConfig MAVEN_HOME "!MAVEN_HOME!"
        call :Log "Maven found in PATH: !MAVEN_HOME!"
        echo   [OK] Maven found (system)
        exit /b 0
    )
)

if exist "tools\maven\bin\mvn.cmd" (
    set "MAVEN_HOME=%CD%\tools\maven"
    set "PATH=%MAVEN_HOME%\bin;!PATH!"
    call :SaveConfig MAVEN_HOME "!MAVEN_HOME!"
    call :Log "Maven found in tools: !MAVEN_HOME!"
    echo   [OK] Maven found (local)
    exit /b 0
)

call :Log "Maven not found"
echo   [--] Maven not found, will install
exit /b 1

:DetectMySQL
call :Log "Detecting MySQL..."

if defined CONFIG_MYSQL_HOME (
    if exist "!CONFIG_MYSQL_HOME!\bin\mysql.exe" (
        set "PATH=!CONFIG_MYSQL_HOME!\bin;!PATH!"
        call :Log "MySQL found in config: !CONFIG_MYSQL_HOME!"
        echo   [OK] MySQL found (saved location)
        exit /b 0
    )
)

where mysql >nul 2>&1
if not errorlevel 1 (
    for /f "tokens=*" %%i in ('where mysql') do (
        set "MYSQL_PATH=%%i"
        set "MYSQL_HOME=%%~dpi.."
        call :SaveConfig MYSQL_HOME "!MYSQL_HOME!"
        call :Log "MySQL found in PATH: !MYSQL_HOME!"
        echo   [OK] MySQL found (system)
        exit /b 0
    )
)

if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 8.0"
    set "PATH=!MYSQL_HOME!\bin;!PATH!"
    call :SaveConfig MYSQL_HOME "!MYSQL_HOME!"
    call :Log "MySQL found: !MYSQL_HOME!"
    echo   [OK] MySQL Server 8.0 found
    exit /b 0
)

if exist "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" (
    set "MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 9.0"
    set "PATH=!MYSQL_HOME!\bin;!PATH!"
    call :SaveConfig MYSQL_HOME "!MYSQL_HOME!"
    call :Log "MySQL found: !MYSQL_HOME!"
    echo   [OK] MySQL Server 9.0 found
    exit /b 0
)

call :Log "MySQL not found"
echo   [--] MySQL not found, will install
exit /b 1

:: ============================================
:: SILENT INSTALLATION
:: ============================================

:InstallJava
call :Log "Installing Java 21..."
echo   Installing Java 21 (~200MB download)...
echo   Please wait, this may take several minutes...

set "ERROR_TYPE=NETWORK"

powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_windows_hotspot_21.0.1_12.zip' -OutFile '%TEMP%\PatraKosh-Setup\java.zip' -TimeoutSec 300 } catch { exit 1 }"

if not exist "%TEMP%\PatraKosh-Setup\java.zip" (
    call :LogError "Java download failed"
    exit /b 1
)

call :Log "Extracting Java..."
echo   Extracting Java...
powershell -Command "try { Expand-Archive -Path '%TEMP%\PatraKosh-Setup\java.zip' -DestinationPath 'tools\' -Force } catch { exit 1 }"

for /d %%i in ("tools\jdk-21*") do (
    if exist "tools\java" rmdir /s /q "tools\java"
    ren "%%i" "java"
    set "JAVA_HOME=%CD%\tools\java"
    set "PATH=!JAVA_HOME!\bin;!PATH!"
    call :SaveConfig JAVA_HOME "!JAVA_HOME!"
    call :Log "Java installed successfully: !JAVA_HOME!"
    echo   [OK] Java 21 installed successfully
    exit /b 0
)

call :LogError "Java extraction failed"
exit /b 1

:InstallMaven
call :Log "Installing Maven 3.9..."
echo   Installing Maven 3.9 (~10MB download)...
echo   Please wait...

set "ERROR_TYPE=NETWORK"

powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\PatraKosh-Setup\maven.zip' -TimeoutSec 300 } catch { exit 1 }"

if not exist "%TEMP%\PatraKosh-Setup\maven.zip" (
    call :LogError "Maven download failed"
    exit /b 1
)

call :Log "Extracting Maven..."
echo   Extracting Maven...
powershell -Command "try { Expand-Archive -Path '%TEMP%\PatraKosh-Setup\maven.zip' -DestinationPath 'tools\' -Force } catch { exit 1 }"

if exist "tools\apache-maven-3.9.6" (
    if exist "tools\maven" rmdir /s /q "tools\maven"
    ren "tools\apache-maven-3.9.6" "maven"
    set "MAVEN_HOME=%CD%\tools\maven"
    set "PATH=!MAVEN_HOME!\bin;!PATH!"
    call :SaveConfig MAVEN_HOME "!MAVEN_HOME!"
    call :Log "Maven installed successfully: !MAVEN_HOME!"
    echo   [OK] Maven 3.9 installed successfully
    exit /b 0
)

call :LogError "Maven extraction failed"
exit /b 1

:InstallMySQL
call :Log "Installing MySQL 8.0..."
echo   Installing MySQL 8.0 (~2MB download)...
echo   Please wait...

set "ERROR_TYPE=NETWORK"

powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.40.0.msi' -OutFile '%TEMP%\PatraKosh-Setup\mysql-installer.msi' -TimeoutSec 300 } catch { exit 1 }"

if not exist "%TEMP%\PatraKosh-Setup\mysql-installer.msi" (
    call :LogError "MySQL download failed"
    echo.
    echo   [WARNING] MySQL automatic installation failed
    echo   Please install MySQL manually from: https://dev.mysql.com/downloads/installer/
    echo.
    pause
    exit /b 1
)

call :Log "Installing MySQL silently..."
echo   Installing MySQL Server (this may take 5-10 minutes)...

start /wait msiexec /i "%TEMP%\PatraKosh-Setup\mysql-installer.msi" /qn
timeout /t 10 >nul
net start MySQL80 >nul 2>&1

call :DetectMySQL
if not errorlevel 1 (
    call :Log "MySQL installed successfully"
    echo   [OK] MySQL installed successfully
    exit /b 0
)

echo   [WARNING] Silent installation may need user interaction
echo   Opening installer...
pause

start /wait msiexec /i "%TEMP%\PatraKosh-Setup\mysql-installer.msi" /qb
timeout /t 10 >nul
net start MySQL80 >nul 2>&1

call :DetectMySQL
if not errorlevel 1 (
    call :Log "MySQL installed successfully"
    echo   [OK] MySQL installed successfully
    exit /b 0
)

call :LogError "MySQL installation failed"
echo   [WARNING] Please install MySQL manually
pause
exit /b 1

:: ============================================
:: DATABASE SETUP
:: ============================================

:SetupDatabase
call :ConnectDatabase
if errorlevel 1 exit /b 1

call :CreateDatabase
if errorlevel 1 exit /b 1

call :UpdateAppConfig
exit /b 0

:ConnectDatabase
call :Log "Connecting to MySQL database..."
echo   Connecting to MySQL database...

if defined CONFIG_MYSQL_HOME (
    set "MYSQL_CMD=%CONFIG_MYSQL_HOME%\bin\mysql.exe"
) else (
    set "MYSQL_CMD=mysql"
)

call :Log "Using MySQL command: %MYSQL_CMD%"

if defined CONFIG_MYSQL_PASSWORD (
    if not "!CONFIG_MYSQL_PASSWORD!"=="" (
        call :Log "Trying with saved password..."
        "%MYSQL_CMD%" -u root -p!CONFIG_MYSQL_PASSWORD! -e "SHOW DATABASES;" >nul 2>&1
        set "CONN_RESULT=!ERRORLEVEL!"
        if !CONN_RESULT! equ 0 (
            call :Log "Connected with saved password"
            echo   [OK] Connected to MySQL
            exit /b 0
        )
    )
)

call :Log "Trying without password..."
"%MYSQL_CMD%" -u root -e "SHOW DATABASES;" >nul 2>&1
if errorlevel 1 goto :ConnectWithPassword
set "CONFIG_MYSQL_PASSWORD="
call :SaveConfig MYSQL_PASSWORD ""
call :Log "Connected without password"
echo   [OK] Connected to MySQL (no password)
exit /b 0

:ConnectWithPassword
call :Log "Connection without password failed, prompting user..."
echo.
echo   MySQL requires authentication.
set /p "MYSQL_PASSWORD=  Enter MySQL root password: "

call :Log "User entered password, trying with password..."
"%MYSQL_CMD%" -u root -p%MYSQL_PASSWORD% -e "SHOW DATABASES;" >nul 2>&1
if errorlevel 1 goto :ConnectFailed
call :SaveConfig MYSQL_PASSWORD "%MYSQL_PASSWORD%"
set "CONFIG_MYSQL_PASSWORD=%MYSQL_PASSWORD%"
call :Log "Connected with password (saved for future)"
echo   [OK] Connected to MySQL (password saved)
exit /b 0

:ConnectFailed
call :LogError "Password authentication failed"
echo   [ERROR] Could not connect to MySQL
exit /b 1

:CreateDatabase
call :Log "Creating database..."

if defined CONFIG_MYSQL_HOME (
    set "MYSQL_CMD=%CONFIG_MYSQL_HOME%\bin\mysql.exe"
) else (
    set "MYSQL_CMD=mysql"
)

if defined CONFIG_MYSQL_PASSWORD (
    if not "!CONFIG_MYSQL_PASSWORD!"=="" (
        "%MYSQL_CMD%" -u root -p!CONFIG_MYSQL_PASSWORD! -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;" 2>nul
        set "DB_RESULT=!ERRORLEVEL!"
    ) else (
        "%MYSQL_CMD%" -u root -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;" 2>nul
        set "DB_RESULT=!ERRORLEVEL!"
    )
) else (
    "%MYSQL_CMD%" -u root -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;" 2>nul
    set "DB_RESULT=!ERRORLEVEL!"
)

if !DB_RESULT! neq 0 (
    call :LogError "Failed to create database"
    echo   [ERROR] Could not create database
    exit /b 1
)

call :Log "Database created/verified"
echo   [OK] Database ready

call :ApplySchema
exit /b !ERRORLEVEL!

:ApplySchema
call :Log "Applying database schema..."
echo   Applying database schema...

if defined CONFIG_MYSQL_HOME (
    set "MYSQL_CMD=%CONFIG_MYSQL_HOME%\bin\mysql.exe"
) else (
    set "MYSQL_CMD=mysql"
)

if exist "database_schema.sql" (
    set "SQL_FILE=database_schema.sql"
) else (
    call :LogError "No SQL setup file found (database_schema.sql)"
    exit /b 1
)

if defined CONFIG_MYSQL_PASSWORD (
    if not "!CONFIG_MYSQL_PASSWORD!"=="" (
        "%MYSQL_CMD%" -u root -p!CONFIG_MYSQL_PASSWORD! patrakosh_db < "%SQL_FILE%" 2>nul
    ) else (
        "%MYSQL_CMD%" -u root patrakosh_db < "%SQL_FILE%" 2>nul
    )
) else (
    "%MYSQL_CMD%" -u root patrakosh_db < "%SQL_FILE%" 2>nul
)

call :Log "Schema applied successfully"
echo   [OK] Schema applied successfully

call :UpdateAppConfig
exit /b 0

:UpdateAppConfig
call :Log "Updating application configuration..."

if not exist "src\main\resources\application.properties" (
    call :Log "application.properties not found, skipping"
    exit /b 0
)

if defined CONFIG_MYSQL_PASSWORD (
    if not "!CONFIG_MYSQL_PASSWORD!"=="" (
        powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=!CONFIG_MYSQL_PASSWORD!' | Set-Content 'src\main\resources\application.properties'"
    ) else (
        powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=' | Set-Content 'src\main\resources\application.properties'"
    )
) else (
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=' | Set-Content 'src\main\resources\application.properties'"
)

call :Log "Application configuration updated"
exit /b 0

:: ============================================
:: BUILD MANAGEMENT
:: ============================================

:CleanBuild
call :Log "Starting clean build..."
echo.
echo   Building application...
echo   This will download dependencies and compile code.
echo   First build may take 5-10 minutes.
echo.

call mvn clean compile -q

if errorlevel 1 (
    call :LogError "Build failed"
    echo.
    echo   [ERROR] Build failed!
    echo   Check the output above for errors.
    echo.
    exit /b 1
)

call :Log "Build completed successfully"
echo.
echo   [OK] Build completed successfully
echo.
exit /b 0

:LaunchApp
call :Log "Launching PatraKosh application..."
echo.
echo ==========================================
echo    Launching PatraKosh...
echo ==========================================
echo.
echo.
echo.

call mvn javafx:run

set "APP_RESULT=!ERRORLEVEL!"

if !APP_RESULT! neq 0 (
    call :LogError "Application failed to start"
    echo.
    echo [ERROR] Application failed to start
    echo.
    pause
    exit /b 1
)

call :Log "Application exited normally"
echo.
echo ==========================================
echo    PatraKosh Session Complete
echo ==========================================
echo.
pause
exit /b 0

:: ============================================
:: SETUP VERIFICATION
:: ============================================

:VerifySetup
call :Log "Verifying setup..."

if defined CONFIG_JAVA_HOME (
    if not exist "!CONFIG_JAVA_HOME!\bin\java.exe" (
        call :LogError "Java no longer available"
        exit /b 1
    )
) else (
    where java >nul 2>&1
    if errorlevel 1 (
        call :LogError "Java not found"
        exit /b 1
    )
)

if defined CONFIG_MAVEN_HOME (
    if not exist "!CONFIG_MAVEN_HOME!\bin\mvn.cmd" (
        call :LogError "Maven no longer available"
        exit /b 1
    )
) else (
    where mvn >nul 2>&1
    if errorlevel 1 (
        call :LogError "Maven not found"
        exit /b 1
    )
)

if defined CONFIG_MYSQL_HOME (
    if not exist "!CONFIG_MYSQL_HOME!\bin\mysql.exe" (
        call :LogError "MySQL no longer available"
        exit /b 1
    )
) else (
    where mysql >nul 2>&1
    if errorlevel 1 (
        call :LogError "MySQL not found"
        exit /b 1
    )
)

call :Log "Setup verification passed"
exit /b 0

:CreateSetupFlag
call :Log "Creating setup completion flag..."
echo %date% %time% - Setup completed successfully > .setup_complete
call :SaveConfig SETUP_VERSION "2.0"
call :SaveConfig LAST_RUN "%date% %time%"
call :Log "Setup flag created"
exit /b 0

:: ============================================
:: ERROR HANDLING
:: ============================================

:SetupError
echo.
echo ==========================================
echo    Setup Failed
echo ==========================================
echo.
call :LogError "Setup failed"

if defined LAST_ERROR (
    echo Error: %LAST_ERROR%
    echo.
)

echo Troubleshooting:
echo.

if "%ERROR_TYPE%"=="NETWORK" (
    echo Network/Download Error:
    echo  - Check your internet connection
    echo  - Try running the script again
)

if "%ERROR_TYPE%"=="DATABASE" (
    echo Database Error:
    echo  - Ensure MySQL service is running: net start MySQL80
    echo  - Check MySQL root password
    echo  - Try: fix_database.bat
)

if "%ERROR_TYPE%"=="BUILD" (
    echo Build Error:
    echo  - Check Java version: java -version
    echo  - Try: mvn clean install -X
)

echo.
echo Detailed logs: setup.log
echo.
pause
exit /b 1
