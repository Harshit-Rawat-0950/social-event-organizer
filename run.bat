@echo off
setlocal EnableDelayedExpansion
title Social Event Organizer
rem Lets the console show the box and symbol characters
chcp 65001 >nul

rem The app reads its config and data using paths relative to this folder
cd /d "%~dp0Project Folder" || goto :failed

where javac >nul 2>nul
if errorlevel 1 (
    echo Could not find javac. Install a JDK ^(21 or newer^) and add it to PATH.
    goto :failed
)

echo Compiling...
set "SOURCES=%TEMP%\social_event_organizer_sources.txt"
if exist "%SOURCES%" del "%SOURCES%"
for /r src %%F in (*.java) do (
    if /i not "%%~nF"=="package-info" (
        set "FILE=%%F"
        >>"%SOURCES%" echo "!FILE:\=/!"
    )
)

javac -nowarn -encoding UTF-8 -d bin "@%SOURCES%"
if errorlevel 1 (
    echo.
    echo Compilation failed.
    goto :failed
)
del "%SOURCES%"

java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp bin UserMangerSubSystem.AppStartUp

echo.
pause
exit /b 0

:failed
echo.
pause
exit /b 1
