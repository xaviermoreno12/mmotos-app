@echo off
title M MOTOS CORE - Iniciando...
echo.
echo  ============================================
echo   M MOTOS CORE
echo  ============================================
echo.

REM Crear base de datos si no existe
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -c "CREATE DATABASE mmotos;" 2>nul

REM Arrancar la aplicacion en segundo plano
echo  Iniciando servidor...
start /B javaw -jar "%~dp0mmotos-app-1.0.0-SNAPSHOT.jar"

REM Esperar a que levante
echo  Esperando que la aplicacion levante (20 seg)...
timeout /t 20 /nobreak >nul

REM Abrir en modo app (sin barra de navegador) — da sensacion de app nativa
echo  Abriendo M MOTOS CORE...

REM Intentar Chrome primero
if exist "%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe" (
    start "" "%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe" --app=http://localhost:8080 --start-maximized --disable-infobars
    goto :fin
)

REM Intentar Chrome en Program Files
if exist "%PROGRAMFILES%\Google\Chrome\Application\chrome.exe" (
    start "" "%PROGRAMFILES%\Google\Chrome\Application\chrome.exe" --app=http://localhost:8080 --start-maximized --disable-infobars
    goto :fin
)

REM Intentar Edge
if exist "%PROGRAMFILES(X86)%\Microsoft\Edge\Application\msedge.exe" (
    start "" "%PROGRAMFILES(X86)%\Microsoft\Edge\Application\msedge.exe" --app=http://localhost:8080 --start-maximized
    goto :fin
)

REM Fallback: browser por defecto
start http://localhost:8080

:fin
echo  M MOTOS CORE corriendo en http://localhost:8080
