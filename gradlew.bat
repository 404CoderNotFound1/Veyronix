@echo off
setlocal
set "GRADLE_VERSION=8.13"
set "GRADLE_SHA256=20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78"
if defined GRADLE_USER_HOME (
  set "BASE_DIR=%GRADLE_USER_HOME%\portfolio-bootstrap"
) else (
  set "BASE_DIR=%USERPROFILE%\.gradle\portfolio-bootstrap"
)
set "INSTALL_DIR=%BASE_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP_FILE=%BASE_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
set "URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%INSTALL_DIR%\bin\gradle.bat" (
  echo Gradle %GRADLE_VERSION% is not cached; downloading the official distribution...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop'; New-Item -ItemType Directory -Force -Path '%BASE_DIR%' | Out-Null; Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%ZIP_FILE%'; $h=(Get-FileHash -Algorithm SHA256 '%ZIP_FILE%').Hash.ToLower(); if($h -ne '%GRADLE_SHA256%'){ throw 'Gradle checksum mismatch' }; if(Test-Path '%INSTALL_DIR%'){Remove-Item -Recurse -Force '%INSTALL_DIR%'}; Expand-Archive -Force '%ZIP_FILE%' '%BASE_DIR%'"
  if errorlevel 1 exit /b 1
)

call "%INSTALL_DIR%\bin\gradle.bat" %*
endlocal
