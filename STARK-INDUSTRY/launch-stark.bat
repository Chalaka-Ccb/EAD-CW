@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1
if errorlevel 1 (
 echo Java 17 or later is required. Install Java and add it to PATH.
 pause
 exit /b 1
)
if not exist "STARK-INDUSTRY-1.0.jar" (
 echo Missing STARK-INDUSTRY-1.0.jar in this folder.
 echo Run mvnw.cmd clean package and copy the JAR from target.
 pause
 exit /b 1
)
java -jar "STARK-INDUSTRY-1.0.jar"
if errorlevel 1 pause
