@echo off
chcp 65001 >nul
title Preparar Memoria KINGSTON - Llaves USB y Apps para B866V2
cls
echo ================================================================================
echo   PREPARAR MEMORIA USB (KINGSTON) CON LLAVES ZTE Y APPS ANDROID TV
echo ================================================================================
echo.

set "ROOT_DIR=C:\Users\andre\OneDrive\Desktop\xview"
set "QA_SRC=%ROOT_DIR%\experimento\12_KIT_USB_QADEBUG_Y_EXTRACCION\ARCHIVOS_PARA_RAIZ_USB"
set "APK_SRC=%ROOT_DIR%\experimento\04_apks_dispositivo"
set "USB_DRV="

for /f "usebackq tokens=*" %%D in (`powershell -NoProfile -Command "$v = Get-Volume | Where-Object { $_.FileSystemLabel -eq 'KINGSTON' -or $_.DriveType -eq 'Removable' } | Select-Object -First 1; if ($v -and $v.DriveLetter) { Write-Output ($v.DriveLetter + ':') }"`) do (
    set "USB_DRV=%%D"
)

if "%USB_DRV%"=="" (
    set /p USB_DRV="Ingresa la letra de tu memoria KINGSTON (ejemplo D: o E:): "
)

echo Memoria USB detectada en: %USB_DRV%\
echo.
echo [1/2] Copiando llaves de configuracion y diagnostico ZTE a la raiz de %USB_DRV%\ ...
xcopy /E /Y /I "%QA_SRC%\*" "%USB_DRV%\" >nul

echo [2/2] Copiando instaladores de aplicaciones (.apk) a %USB_DRV%\APPS_ANDROID_TV\ ...
if not exist "%USB_DRV%\APPS_ANDROID_TV" mkdir "%USB_DRV%\APPS_ANDROID_TV"
copy /Y "%APK_SRC%\Filemanager.apk" "%USB_DRV%\APPS_ANDROID_TV\" >nul
copy /Y "%APK_SRC%\DLStore.apk" "%USB_DRV%\APPS_ANDROID_TV\" >nul
copy /Y "%APK_SRC%\AppDMC.apk" "%USB_DRV%\APPS_ANDROID_TV\" >nul
copy /Y "%APK_SRC%\Hdplay.apk" "%USB_DRV%\APPS_ANDROID_TV\" >nul
if exist "%ROOT_DIR%\com.esaba.downloader-2-0-4-ForGoogleAndroidDevices-arm64-v8a-android60.apk" (
    copy /Y "%ROOT_DIR%\com.esaba.downloader-2-0-4-ForGoogleAndroidDevices-arm64-v8a-android60.apk" "%USB_DRV%\APPS_ANDROID_TV\Downloader.apk" >nul
)
if exist "%ROOT_DIR%\xuper hydra.apk" (
    copy /Y "%ROOT_DIR%\xuper hydra.apk" "%USB_DRV%\APPS_ANDROID_TV\" >nul
)

echo.
echo ================================================================================
echo   LISTO: MEMORIA %USB_DRV%\ PREPARADA CORRECTAMENTE
echo ================================================================================
echo   Archivos instalados en %USB_DRV%\ :
echo     - QADEBUG.ini y QADEBUGTOOL.ini (Llaves de servicio y diagnostico USB)
echo     - logconfig.ini y LOG_ZTESTB\logconfig.ini (Volcado de propiedades del sistema)
echo     - APPS_ANDROID_TV\Filemanager.apk, DLStore.apk, AppDMC.apk, Hdplay.apk, Downloader.apk
echo ================================================================================
pause
