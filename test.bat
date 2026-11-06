@echo off
rem Runs the JUnit tests. Your real data files are never touched.
rem   test.bat                   runs everything
rem   test.bat EventFacadeTest   runs one test class
setlocal EnableDelayedExpansion
title Social Event Organizer - Tests
chcp 65001 >nul

cd /d "%~dp0Project Folder" || goto :failed

where javac >nul 2>nul
if errorlevel 1 (
    echo Could not find javac. Install a JDK ^(21 or newer^) and add it to PATH.
    goto :failed
)

rem Use the JUnit that comes with Eclipse, or download it once if there is none
set "JUNIT="
set "HAMCREST="
for %%J in ("%USERPROFILE%\.p2\pool\plugins\org.junit_4*.jar") do set "JUNIT=%%~fJ"
for %%J in ("%USERPROFILE%\.p2\pool\plugins\org.hamcrest_*.jar") do set "HAMCREST=%%~fJ"
if not defined JUNIT if exist "lib\junit-4.13.2.jar" set "JUNIT=%CD%\lib\junit-4.13.2.jar"
if not defined HAMCREST if exist "lib\hamcrest-core-1.3.jar" set "HAMCREST=%CD%\lib\hamcrest-core-1.3.jar"
if not defined JUNIT (
    echo Downloading JUnit...
    if not exist lib mkdir lib
    curl -sSfL -o "lib\junit-4.13.2.jar" "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar" || goto :nojunit
    set "JUNIT=%CD%\lib\junit-4.13.2.jar"
)
if not defined HAMCREST (
    if not exist lib mkdir lib
    curl -sSfL -o "lib\hamcrest-core-1.3.jar" "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar" || goto :nojunit
    set "HAMCREST=%CD%\lib\hamcrest-core-1.3.jar"
)

echo Compiling...
if exist build\test-classes rmdir /s /q build\test-classes
mkdir build\test-classes
set "SOURCES=%CD%\build\test-sources.txt"
if exist "%SOURCES%" del "%SOURCES%"
for /r src %%F in (*.java) do (
    set "FILE=%%F"
    if /i not "%%~nF"=="package-info" >>"%SOURCES%" echo "!FILE:\=/!"
)
for /r test %%F in (*.java) do (
    set "FILE=%%F"
    >>"%SOURCES%" echo "!FILE:\=/!"
)
javac -nowarn -encoding UTF-8 -cp "%JUNIT%;%HAMCREST%" -d build\test-classes "@%SOURCES%"
if errorlevel 1 (
    echo.
    echo Compilation failed.
    goto :failed
)

java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "build\test-classes;%JUNIT%;%HAMCREST%" TestSupport.PrettyRunner %*
set "RESULT=%ERRORLEVEL%"
echo.
if not defined NO_PAUSE pause
exit /b %RESULT%

:nojunit
echo Could not get JUnit. Check your internet connection, or run the tests from Eclipse.
:failed
echo.
if not defined NO_PAUSE pause
exit /b 1
