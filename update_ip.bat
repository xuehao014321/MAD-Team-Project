@echo off
chcp 65001 >nul
echo ===============================================
echo      Dynamic IP Detection and Android Config Tool
echo ===============================================
echo.

echo Getting local WiFi IP address...
node update_android_ip.js

echo.
echo ===============================================
echo Update Complete!
echo ===============================================
echo.
echo Instructions:
echo 1. Double-click this file to automatically update Android project IP config
echo 2. Rebuild the Android project after updating
echo 3. Ensure your API server runs on the same IP and port
echo.
pause 