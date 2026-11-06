@echo off
setlocal

set "MAVEN_VERSION=3.9.9"
set "WRAPPER_DIR=%~dp0.mvn\wrapper"
set "MAVEN_CACHE=%TEMP%\maven-wrapper"
set "MAVEN_HOME=%MAVEN_CACHE%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Maven %MAVEN_VERSION% was not found. Downloading it...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'"
    if errorlevel 1 (
        echo Could not download Maven. Check your internet connection.
        exit /b 1
    )

    if not exist "%MAVEN_CACHE%" mkdir "%MAVEN_CACHE%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_CACHE%' -Force"
    if errorlevel 1 (
        echo Could not extract Maven.
        exit /b 1
    )
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%