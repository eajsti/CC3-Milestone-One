@echo off
echo ========================================
echo   Smart Parking System - Test Script
echo ========================================
echo.

cd src
echo Compiling Java files...
javac -cp "..\lib\sqlite-jdbc-3.51.3.0.jar" -d . *.java

echo.
echo Running system...
echo ========================================
echo.

java -cp ".;..\lib\sqlite-jdbc-3.51.3.0.jar" Main

pause