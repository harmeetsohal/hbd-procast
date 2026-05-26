@echo off
echo ==================================================
echo   HBD PROCAST ProBill - APK Builder (Windows)
echo ==================================================

:: Auto-detect Android SDK
if exist "%LOCALAPPDATA%\Android\Sdk" (
    set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
) else if exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
    set ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk
) else (
    echo ERROR: Android SDK not found. Install Android Studio first.
    exit /b 1
)

echo Android SDK: %ANDROID_HOME%
echo sdk.dir=%ANDROID_HOME% > local.properties

echo Building APK...
call gradlew.bat assembleDebug assembleRelease

echo.
echo BUILD COMPLETE!
echo Debug APK: app\build\outputs\apk\debug\app-debug.apk
echo Release APK: app\build\outputs\apk\release\app-release-unsigned.apk
pause
