@echo off
chcp 65001 >nul
title Flasheo Validado y Seguro ZTE B866V2 Android 12 (Preservando Widevine L1 y MAC)
cls
echo ================================================================================
echo   PASO 1: VALIDACION CRIPTOGRAFICA PREVIA DEL FIRMWARE B866V2 ANDROID 12
echo ================================================================================
powershell -ExecutionPolicy Bypass -File "%~dp0VALIDAR_FIRMWARE_Y_OTA_ZTE.ps1"
if errorlevel 1 (
    echo [ERROR] La validacion previa fallo. Abortando por seguridad.
    pause
    exit /b 1
)

echo.
echo ================================================================================
echo   PASO 2: PREPARACION DE FLASHEO SEGURO (CONSERVANDO FACTORY / WIDEVINE L1)
echo ================================================================================
echo   Este script instala el firmware Android 12 en tu caja Xview (ZTE B866V2)
echo   SIN sobrescribir las particiones 'factory' (Widevine L1 / HDCP) ni 'cri_data'
echo   (MAC / Numero de Serie original de tu caja).
echo ================================================================================
echo.
set /p CONFIRM="¿Deseas continuar cuando tengas la caja en modo Fastboot? (S/N): "
if /I not "%CONFIRM%"=="S" exit /b 0

cd /d "c:\Users\CORSAIR\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2"

echo [1/11] Detectando dispositivo en Fastboot...
fastboot devices
timeout /t 2 /nobreak >nul

echo [2/11] Desbloqueando bootloader (lock = 10100000 para aceptar vbmeta Flags=0x03)...
fastboot oem 'setenv lock 10100000;save
timeout /t 2 /nobreak >nul
fastboot reboot bootloader
timeout /t 4 /nobreak >nul
fastboot getvar unlocked

echo [3/11] Flasheando boot.img (100%% Oficial ZTE verificado)...
fastboot flash boot boot.img

echo [4/11] Flasheando _aml_dtb y dtbo.img (100%% Oficial ZTE verificado)...
fastboot flash _aml_dtb dtb.img
fastboot flash dtbo dtbo.img

echo [5/11] OMITIENDO factory.img y cri_data.img para conservar tu Widevine L1 y MAC originales...

echo [6/11] Flasheando logo.img y param.img...
fastboot flash logo logo.img
fastboot flash param param.img

echo [7/11] Flasheando conf.img y limpiando oem.img...
fastboot flash conf conf.img
fastboot flash oem oem.img

echo [8/11] Flasheando super.img (Android 12 liberado)...
fastboot flash super super.img

echo [9/11] Flasheando vbmeta.img (Flags=0x03 dm-verity desactivado)...
fastboot flash vbmeta vbmeta.img

echo [10/11] Flasheando recovery.img (100%% Oficial ZTE verificado)...
fastboot flash recovery recovery.img

echo [11/11] Limpiando userdata y cache...
fastboot erase userdata
fastboot erase cache
fastboot reboot

echo.
echo [COMPLETADO] Tu caja Xview B866V2 se reiniciara en Android 12 conservando sus llaves DRM.
pause
