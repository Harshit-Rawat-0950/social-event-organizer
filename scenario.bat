@echo off
rem End-to-end check: creates users, relationships and events by typing into the
rem real app, then verifies who sees what and how. Uses its own sandbox folder and
rem never touches your real data.
rem   scenario.bat             runs the checks
rem   scenario.bat --explore   runs the checks, then opens the app on that sandbox
setlocal EnableDelayedExpansion
title Social Event Organizer - Scenario
chcp 65001 >nul

cd /d "%~dp0Project Folder" || goto :failed

where javac >nul 2>nul
if errorlevel 1 (
    echo Could not find javac. Install a JDK ^(21 or newer^) and add it to PATH.
    goto :failed
)

echo Compiling...
if exist build\scenario-classes rmdir /s /q build\scenario-classes
mkdir build\scenario-classes
set "SOURCES=%CD%\build\scenario-sources.txt"
if exist "%SOURCES%" del "%SOURCES%"
for /r src %%F in (*.java) do (
    set "FILE=%%F"
    if /i not "%%~nF"=="package-info" >>"%SOURCES%" echo "!FILE:\=/!"
)
javac -nowarn -encoding UTF-8 -d build\scenario-classes "@%SOURCES%" test\ScenarioSubSystem\ScenarioRunner.java
if errorlevel 1 (
    echo.
    echo Compilation failed.
    goto :failed
)

java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp build\scenario-classes ScenarioSubSystem.ScenarioRunner %*
set "RESULT=%ERRORLEVEL%"
echo.
if not defined NO_PAUSE pause
exit /b %RESULT%

:failed
echo.
if not defined NO_PAUSE pause
exit /b 1
