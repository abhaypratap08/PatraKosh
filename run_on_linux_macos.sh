#!/bin/bash

# ============================================
# PATRAKOSH ONE-CLICK LAUNCHER
# Fully automated setup, build, and launch
# For Linux and MacOS
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

CONFIG_FILE=".patrakosh.config"
SETUP_FLAG=".setup_complete"
LOG_FILE="setup.log"

# ============================================
# LOGGING SYSTEM
# ============================================

init_log() {
    if [ -f "$LOG_FILE" ]; then
        local size=$(stat -f%z "$LOG_FILE" 2>/dev/null || stat -c%s "$LOG_FILE" 2>/dev/null || echo 0)
        if [ "$size" -gt 1048576 ]; then
            [ -f "setup.old.log" ] && rm "setup.old.log"
            mv "$LOG_FILE" "setup.old.log"
        fi
    fi
    echo "============================================" >> "$LOG_FILE"
    echo "PatraKosh Setup Log - $(date)" >> "$LOG_FILE"
    echo "============================================" >> "$LOG_FILE"
}

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
    [ "$SILENT" != "1" ] && echo "$1"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "$LOG_FILE"
    echo "[ERROR] $1"
    LAST_ERROR="$1"
}

# ============================================
# CONFIGURATION MANAGEMENT
# ============================================

load_config() {
    log "Loading configuration..."
    if [ -f "$CONFIG_FILE" ]; then
        while IFS='=' read -r key value; do
            [ -n "$key" ] && export "CONFIG_$key=$value"
        done < "$CONFIG_FILE"
        log "Configuration loaded successfully"
    else
        log "No configuration file found, will create new"
    fi
}

save_config() {
    local key="$1"
    local value="$2"
    if [ -f "$CONFIG_FILE" ]; then
        grep -v "^$key=" "$CONFIG_FILE" > "$CONFIG_FILE.tmp" 2>/dev/null || true
        mv "$CONFIG_FILE.tmp" "$CONFIG_FILE"
    fi
    echo "$key=$value" >> "$CONFIG_FILE"
    export "CONFIG_$key=$value"
}

# ============================================
# FIRST-TIME SETUP WORKFLOW
# ============================================

run_first_time_setup() {
    clear
    echo ""
    echo "=========================================="
    echo "   PatraKosh v2.0 - First-Time Setup"
    echo "=========================================="
    echo ""
    echo "Setting up your environment automatically..."
    echo "This will take 10-15 minutes."
    echo ""
    echo "What will be installed:"
    echo " - Java 21 (if needed)"
    echo " - Maven 3.9 (if needed)"
    echo " - MySQL 8.0 (if needed)"
    echo " - PatraKosh database"
    echo " - Application dependencies"
    echo ""
    echo "Please wait..."
    echo ""

    log "=== Starting First-Time Setup ==="

    # Create necessary directories
    mkdir -p tools storage logs

    # Step 1: Setup Java
    log "Step 1/6: Setting up Java..."
    echo "[1/6] Setting up Java..."
    detect_java || install_java

    # Step 2: Setup Maven
    log "Step 2/6: Setting up Maven..."
    echo "[2/6] Setting up Maven..."
    detect_maven || install_maven

    # Step 3: Setup MySQL
    log "Step 3/6: Setting up MySQL..."
    echo "[3/6] Setting up MySQL..."
    detect_mysql || install_mysql

    # Step 4: Setup Database
    log "Step 4/6: Setting up database..."
    echo "[4/6] Setting up database..."
    setup_database || { ERROR_TYPE="DATABASE"; setup_error; }

    # Step 5: Build Application
    log "Step 5/6: Building application..."
    echo "[5/6] Building application (this may take 5-10 minutes)..."
    clean_build || { ERROR_TYPE="BUILD"; setup_error; }

    # Step 6: Create completion flag
    log "Step 6/6: Finalizing setup..."
    echo "[6/6] Finalizing setup..."
    create_setup_flag

    echo ""
    echo "=========================================="
    echo "   Setup Complete!"
    echo "=========================================="
    echo ""
    log "=== Setup completed successfully ==="
    echo "Launching PatraKosh..."
    echo ""

    launch_app
}

# ============================================
# QUICK LAUNCH WORKFLOW
# ============================================

run_quick_launch() {
    log "=== Quick Launch Mode ==="
    echo ""
    echo "=========================================="
    echo "   PatraKosh v2.0 - Quick Launch"
    echo "=========================================="
    echo ""
    echo "Verifying environment..."

    verify_setup || {
        log_error "Setup verification failed, re-running full setup"
        rm -f "$SETUP_FLAG"
        run_first_time_setup
        return
    }

    echo "Environment OK"
    echo ""

    echo "Compiling application..."
    mvn clean compile -q || {
        echo "[ERROR] Compilation failed"
        ERROR_TYPE="BUILD"
        setup_error
    }

    echo "[OK] Compilation successful"
    echo ""
    echo "Launching PatraKosh..."
    echo ""
    log "Launching application"

    launch_app
}

# ============================================
# DEPENDENCY DETECTION
# ============================================

detect_java() {
    log "Detecting Java..."

    if [ -n "$CONFIG_JAVA_HOME" ] && [ -x "$CONFIG_JAVA_HOME/bin/java" ]; then
        export JAVA_HOME="$CONFIG_JAVA_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
        log "Java found in config: $JAVA_HOME"
        echo "  [OK] Java found (saved location)"
        return 0
    fi

    if command -v java &> /dev/null; then
        JAVA_HOME="$(dirname $(dirname $(readlink -f $(which java))))"
        save_config "JAVA_HOME" "$JAVA_HOME"
        log "Java found in PATH: $JAVA_HOME"
        echo "  [OK] Java found (system)"
        return 0
    fi

    if [ -x "tools/java/bin/java" ]; then
        JAVA_HOME="$SCRIPT_DIR/tools/java"
        export PATH="$JAVA_HOME/bin:$PATH"
        save_config "JAVA_HOME" "$JAVA_HOME"
        log "Java found in tools: $JAVA_HOME"
        echo "  [OK] Java found (local)"
        return 0
    fi

    log "Java not found"
    echo "  [--] Java not found, will install"
    return 1
}

detect_maven() {
    log "Detecting Maven..."

    if [ -n "$CONFIG_MAVEN_HOME" ] && [ -x "$CONFIG_MAVEN_HOME/bin/mvn" ]; then
        export MAVEN_HOME="$CONFIG_MAVEN_HOME"
        export PATH="$MAVEN_HOME/bin:$PATH"
        log "Maven found in config: $MAVEN_HOME"
        echo "  [OK] Maven found (saved location)"
        return 0
    fi

    if command -v mvn &> /dev/null; then
        MAVEN_HOME="$(dirname $(dirname $(readlink -f $(which mvn))))"
        save_config "MAVEN_HOME" "$MAVEN_HOME"
        log "Maven found in PATH: $MAVEN_HOME"
        echo "  [OK] Maven found (system)"
        return 0
    fi

    if [ -x "tools/maven/bin/mvn" ]; then
        MAVEN_HOME="$SCRIPT_DIR/tools/maven"
        export PATH="$MAVEN_HOME/bin:$PATH"
        save_config "MAVEN_HOME" "$MAVEN_HOME"
        log "Maven found in tools: $MAVEN_HOME"
        echo "  [OK] Maven found (local)"
        return 0
    fi

    log "Maven not found"
    echo "  [--] Maven not found, will install"
    return 1
}

detect_mysql() {
    log "Detecting MySQL..."

    if [ -n "$CONFIG_MYSQL_HOME" ] && [ -x "$CONFIG_MYSQL_HOME/bin/mysql" ]; then
        export PATH="$CONFIG_MYSQL_HOME/bin:$PATH"
        log "MySQL found in config: $CONFIG_MYSQL_HOME"
        echo "  [OK] MySQL found (saved location)"
        return 0
    fi

    if command -v mysql &> /dev/null; then
        MYSQL_HOME="$(dirname $(dirname $(readlink -f $(which mysql))))"
        save_config "MYSQL_HOME" "$MYSQL_HOME"
        log "MySQL found in PATH: $MYSQL_HOME"
        echo "  [OK] MySQL found (system)"
        return 0
    fi

    log "MySQL not found"
    echo "  [--] MySQL not found, will install"
    return 1
}

# ============================================
# INSTALLATION FUNCTIONS
# ============================================

install_java() {
    log "Installing Java 21..."
    echo "  Installing Java 21 (~200MB download)..."
    echo "  Please wait, this may take several minutes..."

    ERROR_TYPE="NETWORK"

    local os_type=$(uname -s)
    local arch=$(uname -m)
    local java_url=""

    if [ "$os_type" = "Darwin" ]; then
        if [ "$arch" = "arm64" ]; then
            java_url="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_aarch64_mac_hotspot_21.0.1_12.tar.gz"
        else
            java_url="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_mac_hotspot_21.0.1_12.tar.gz"
        fi
    else
        java_url="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz"
    fi

    curl -L "$java_url" -o /tmp/java.tar.gz || {
        log_error "Java download failed"
        return 1
    }

    log "Extracting Java..."
    echo "  Extracting Java..."
    mkdir -p tools
    tar -xzf /tmp/java.tar.gz -C tools/ || {
        log_error "Java extraction failed"
        return 1
    }

    mv tools/jdk-* tools/java 2>/dev/null || true
    export JAVA_HOME="$SCRIPT_DIR/tools/java"
    export PATH="$JAVA_HOME/bin:$PATH"
    save_config "JAVA_HOME" "$JAVA_HOME"
    log "Java installed successfully: $JAVA_HOME"
    echo "  [OK] Java 21 installed successfully"
    rm /tmp/java.tar.gz
    return 0
}

install_maven() {
    log "Installing Maven 3.9..."
    echo "  Installing Maven 3.9 (~10MB download)..."
    echo "  Please wait..."

    ERROR_TYPE="NETWORK"

    curl -L "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz" -o /tmp/maven.tar.gz || {
        log_error "Maven download failed"
        return 1
    }

    log "Extracting Maven..."
    echo "  Extracting Maven..."
    mkdir -p tools
    tar -xzf /tmp/maven.tar.gz -C tools/ || {
        log_error "Maven extraction failed"
        return 1
    }

    mv tools/apache-maven-* tools/maven 2>/dev/null || true
    export MAVEN_HOME="$SCRIPT_DIR/tools/maven"
    export PATH="$MAVEN_HOME/bin:$PATH"
    save_config "MAVEN_HOME" "$MAVEN_HOME"
    log "Maven installed successfully: $MAVEN_HOME"
    echo "  [OK] Maven 3.9 installed successfully"
    rm /tmp/maven.tar.gz
    return 0
}

install_mysql() {
    log "Installing MySQL 8.0..."
    echo "  MySQL installation requires manual setup on Linux/MacOS"
    echo ""
    echo "  Please install MySQL using your package manager:"
    echo ""
    
    local os_type=$(uname -s)
    if [ "$os_type" = "Darwin" ]; then
        echo "  macOS (Homebrew):"
        echo "    brew install mysql"
        echo "    brew services start mysql"
    else
        echo "  Ubuntu/Debian:"
        echo "    sudo apt-get update"
        echo "    sudo apt-get install mysql-server"
        echo "    sudo systemctl start mysql"
        echo ""
        echo "  Fedora/RHEL:"
        echo "    sudo dnf install mysql-server"
        echo "    sudo systemctl start mysqld"
    fi
    
    echo ""
    echo "  After installation, run this script again."
    echo ""
    read -p "Press Enter after installing MySQL..."
    
    detect_mysql || {
        log_error "MySQL still not found"
        return 1
    }
    
    return 0
}

# ============================================
# DATABASE SETUP
# ============================================

setup_database() {
    connect_database || return 1
    create_database || return 1
    update_app_config
    return 0
}

connect_database() {
    log "Connecting to MySQL database..."
    echo "  Connecting to MySQL database..."

    local mysql_cmd="mysql"
    if [ -n "$CONFIG_MYSQL_HOME" ]; then
        mysql_cmd="$CONFIG_MYSQL_HOME/bin/mysql"
    fi

    log "Using MySQL command: $mysql_cmd"

    # Try with saved password
    if [ -n "$CONFIG_MYSQL_PASSWORD" ]; then
        log "Trying with saved password..."
        if $mysql_cmd -u root -p"$CONFIG_MYSQL_PASSWORD" -e "SHOW DATABASES;" &>/dev/null; then
            log "Connected with saved password"
            echo "  [OK] Connected to MySQL"
            return 0
        fi
    fi

    # Try without password
    log "Trying without password..."
    if $mysql_cmd -u root -e "SHOW DATABASES;" &>/dev/null; then
        CONFIG_MYSQL_PASSWORD=""
        save_config "MYSQL_PASSWORD" ""
        log "Connected without password"
        echo "  [OK] Connected to MySQL (no password)"
        return 0
    fi

    # Prompt for password
    log "Connection without password failed, prompting user..."
    echo ""
    echo "  MySQL requires authentication."
    read -sp "  Enter MySQL root password: " MYSQL_PASSWORD
    echo ""

    log "User entered password, trying with password..."
    if $mysql_cmd -u root -p"$MYSQL_PASSWORD" -e "SHOW DATABASES;" &>/dev/null; then
        save_config "MYSQL_PASSWORD" "$MYSQL_PASSWORD"
        CONFIG_MYSQL_PASSWORD="$MYSQL_PASSWORD"
        log "Connected with password (saved for future)"
        echo "  [OK] Connected to MySQL (password saved)"
        return 0
    fi

    log_error "Password authentication failed"
    echo "  [ERROR] Could not connect to MySQL"
    return 1
}

create_database() {
    log "Creating database..."

    local mysql_cmd="mysql"
    if [ -n "$CONFIG_MYSQL_HOME" ]; then
        mysql_cmd="$CONFIG_MYSQL_HOME/bin/mysql"
    fi

    local mysql_opts="-u root"
    if [ -n "$CONFIG_MYSQL_PASSWORD" ]; then
        mysql_opts="$mysql_opts -p$CONFIG_MYSQL_PASSWORD"
    fi

    if ! $mysql_cmd $mysql_opts -e "CREATE DATABASE IF NOT EXISTS patrakosh_db;" 2>/dev/null; then
        log_error "Failed to create database"
        echo "  [ERROR] Could not create database"
        return 1
    fi

    log "Database created/verified"
    echo "  [OK] Database ready"

    apply_schema
    return $?
}

apply_schema() {
    log "Applying database schema..."
    echo "  Applying database schema..."

    local mysql_cmd="mysql"
    if [ -n "$CONFIG_MYSQL_HOME" ]; then
        mysql_cmd="$CONFIG_MYSQL_HOME/bin/mysql"
    fi

    if [ ! -f "database_schema.sql" ]; then
        log_error "No SQL setup file found (database_schema.sql)"
        return 1
    fi

    local mysql_opts="-u root"
    if [ -n "$CONFIG_MYSQL_PASSWORD" ]; then
        mysql_opts="$mysql_opts -p$CONFIG_MYSQL_PASSWORD"
    fi

    $mysql_cmd $mysql_opts patrakosh_db < database_schema.sql 2>/dev/null

    log "Schema applied successfully"
    echo "  [OK] Schema applied successfully"

    update_app_config
    return 0
}

update_app_config() {
    log "Updating application configuration..."

    if [ ! -f "src/main/resources/application.properties" ]; then
        log "application.properties not found, skipping"
        return 0
    fi

    if [ -n "$CONFIG_MYSQL_PASSWORD" ]; then
        sed -i.bak "s/db.password=.*/db.password=$CONFIG_MYSQL_PASSWORD/" src/main/resources/application.properties
    else
        sed -i.bak "s/db.password=.*/db.password=/" src/main/resources/application.properties
    fi

    log "Application configuration updated"
    return 0
}

# ============================================
# BUILD MANAGEMENT
# ============================================

clean_build() {
    log "Starting clean build..."
    echo ""
    echo "  Building application..."
    echo "  This will download dependencies and compile code."
    echo "  First build may take 5-10 minutes."
    echo ""

    if ! mvn clean compile -q; then
        log_error "Build failed"
        echo ""
        echo "  [ERROR] Build failed!"
        echo "  Check the output above for errors."
        echo ""
        return 1
    fi

    log "Build completed successfully"
    echo ""
    echo "  [OK] Build completed successfully"
    echo ""
    return 0
}

launch_app() {
    log "Launching PatraKosh application..."
    echo ""
    echo "=========================================="
    echo "   Launching PatraKosh..."
    echo "=========================================="
    echo ""
    echo ""
    echo ""

    mvn javafx:run
    local app_result=$?

    if [ $app_result -ne 0 ]; then
        log_error "Application failed to start"
        echo ""
        echo "[ERROR] Application failed to start"
        echo ""
        read -p "Press Enter to continue..."
        return 1
    fi

    log "Application exited normally"
    echo ""
    echo "=========================================="
    echo "   PatraKosh Session Complete"
    echo "=========================================="
    echo ""
    read -p "Press Enter to continue..."
    return 0
}

# ============================================
# SETUP VERIFICATION
# ============================================

verify_setup() {
    log "Verifying setup..."

    if [ -n "$CONFIG_JAVA_HOME" ]; then
        if [ ! -x "$CONFIG_JAVA_HOME/bin/java" ]; then
            log_error "Java no longer available"
            return 1
        fi
    else
        if ! command -v java &> /dev/null; then
            log_error "Java not found"
            return 1
        fi
    fi

    if [ -n "$CONFIG_MAVEN_HOME" ]; then
        if [ ! -x "$CONFIG_MAVEN_HOME/bin/mvn" ]; then
            log_error "Maven no longer available"
            return 1
        fi
    else
        if ! command -v mvn &> /dev/null; then
            log_error "Maven not found"
            return 1
        fi
    fi

    if [ -n "$CONFIG_MYSQL_HOME" ]; then
        if [ ! -x "$CONFIG_MYSQL_HOME/bin/mysql" ]; then
            log_error "MySQL no longer available"
            return 1
        fi
    else
        if ! command -v mysql &> /dev/null; then
            log_error "MySQL not found"
            return 1
        fi
    fi

    log "Setup verification passed"
    return 0
}

create_setup_flag() {
    log "Creating setup completion flag..."
    echo "$(date) - Setup completed successfully" > "$SETUP_FLAG"
    save_config "SETUP_VERSION" "2.0"
    save_config "LAST_RUN" "$(date)"
    log "Setup flag created"
}

# ============================================
# ERROR HANDLING
# ============================================

setup_error() {
    echo ""
    echo "=========================================="
    echo "   Setup Failed"
    echo "=========================================="
    echo ""
    log_error "Setup failed"

    if [ -n "$LAST_ERROR" ]; then
        echo "Error: $LAST_ERROR"
        echo ""
    fi

    echo "Troubleshooting:"
    echo ""

    case "$ERROR_TYPE" in
        NETWORK)
            echo "Network/Download Error:"
            echo " - Check your internet connection"
            echo " - Try running the script again"
            ;;
        DATABASE)
            echo "Database Error:"
            echo " - Ensure MySQL service is running"
            echo " - Check MySQL root password"
            echo " - Try: sudo systemctl start mysql (Linux)"
            echo " - Try: brew services start mysql (macOS)"
            ;;
        BUILD)
            echo "Build Error:"
            echo " - Check Java version: java -version"
            echo " - Try: mvn clean install -X"
            ;;
    esac

    echo ""
    echo "Detailed logs: $LOG_FILE"
    echo ""
    read -p "Press Enter to exit..."
    exit 1
}

# ============================================
# MAIN ENTRY POINT
# ============================================

main() {
    # Initialize logging
    init_log

    # Load configuration
    load_config

    # Kill any running Java instances
    pkill -f "java.*javafx" 2>/dev/null || true
    sleep 1

    # Check if this is first run or quick launch
    if [ -f "$SETUP_FLAG" ]; then
        run_quick_launch
    else
        run_first_time_setup
    fi
}

# Run main function
main
