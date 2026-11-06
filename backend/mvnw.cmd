@IF "%DEBUG%" == "" @ECHO OFF
SETLOCAL

SET "BASE_DIR=%~dp0"
IF "%BASE_DIR%" == "" SET "BASE_DIR=."
IF "%BASE_DIR:~-1%"=="\" SET "BASE_DIR=%BASE_DIR:~0,-1%"

REM Prefer JDK 22 or JAVA_HOME to ensure Spring Boot 3 compatibility (Java 17+)
SET "JAVACMD="
IF EXIST "C:\Program Files\Java\jdk-22\bin\java.exe" (
    SET "JAVACMD=C:\Program Files\Java\jdk-22\bin\java.exe"
)
IF "%JAVACMD%"=="" IF NOT "%JAVA_HOME%"=="" (
    IF EXIST "%JAVA_HOME%\bin\java.exe" SET "JAVACMD=%JAVA_HOME%\bin\java.exe"
)
IF "%JAVACMD%"=="" SET "JAVACMD=java"

SET "WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

"%JAVACMD%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -cp "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
IF ERRORLEVEL 1 EXIT /B %ERRORLEVEL%
