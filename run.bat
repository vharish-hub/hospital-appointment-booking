@echo off
echo =====================================================================
echo    MediCare Hospital Appointment Booking System - Application Launcher
echo =====================================================================
echo.

:: 1. Detect JDK and configure JAVA_HOME dynamically
set "FOUND_JAVA_HOME="

:: Loop through java.exe locations on PATH to find a valid JDK
for /f "delims=" %%i in ('where java 2^>nul') do (
    call :check_jdk "%%i"
    if defined FOUND_JAVA_HOME goto jdk_configured
)

:: If not found on PATH, check standard installation directories
if not exist "%FOUND_JAVA_HOME%\bin\javac.exe" (
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
        if exist "%%d\bin\javac.exe" (
            set "FOUND_JAVA_HOME=%%d"
            goto jdk_configured
        )
    )
    for /d %%d in ("C:\Program Files\Java\jdk-*") do (
        if exist "%%d\bin\javac.exe" (
            set "FOUND_JAVA_HOME=%%d"
            goto jdk_configured
        )
    )
)

:jdk_configured
if defined FOUND_JAVA_HOME (
    set "JAVA_HOME=%FOUND_JAVA_HOME%"
    set "PATH=%FOUND_JAVA_HOME%\bin;%PATH%"
    echo [INFO] Dynamically configured JAVA_HOME to: %FOUND_JAVA_HOME%
) else (
    echo [WARNING] No JDK installation with javac.exe was detected.
    echo If execution fails, please set the JAVA_HOME system environment variable manually.
)

:: 2. Check if system mvn is available in PATH
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] System Maven detected.
    echo Starting MediCare Server...
    call mvn spring-boot:run
    goto end
)

:: 3. Check if local Maven already exists
if exist "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    echo [INFO] Local Maven detected.
    echo Starting MediCare Server...
    call "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
    goto end
)

:: 4. Create .maven folder and download Maven using curl
echo [INFO] Maven not found. Downloading Apache Maven 3.9.6...
if not exist "%~dp0.maven" mkdir "%~dp0.maven"

curl -L -o "%~dp0.maven\maven.zip" "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to download Maven using curl.
    goto error
)

:: 5. Extract using built-in tar tool
echo [INFO] Extracting Maven...
tar -xf "%~dp0.maven\maven.zip" -C "%~dp0.maven"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to extract Maven zip file.
    goto error
)

:: 6. Clean up downloaded zip
del "%~dp0.maven\maven.zip"

echo [INFO] Maven downloaded and extracted successfully to .maven/
echo Starting MediCare Server...
call "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
goto end

:: Helper Subroutine to check if a java.exe path is a JDK
:check_jdk
set "TMP_BIN=%~dp1"
for %%b in ("%TMP_BIN%..") do set "TMP_HOME=%%~fpb"
if exist "%TMP_HOME%\bin\javac.exe" (
    set "FOUND_JAVA_HOME=%TMP_HOME%"
)
exit /b

:error
echo.
echo [ERROR] Automatic setup failed. Please make sure curl and tar are accessible, or install Maven manually.
pause

:end
