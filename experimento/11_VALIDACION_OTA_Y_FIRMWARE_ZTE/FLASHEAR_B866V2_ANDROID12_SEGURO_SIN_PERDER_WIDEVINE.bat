@echo off
chcp 65001 >nul
title Flasheo Validado y Seguro ZTE B866V2 Android 12 (Con Reparacion ext4 de /cache)
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
echo   PASO 2: PREPARACION DE FLASHEO SEGURO + REPARACION DE PARTICION /CACHE
echo ================================================================================
echo   - Conserva tus particiones 'factory' (Widevine L1 / HDCP) y 'cri_data' (MAC).
echo   - Inyecta cache_ext4.img formateada con mke2fs para eliminar el error:
echo     "E:[libfs_mgr]Failed to mount /cache: Invalid argument"
echo ================================================================================
echo.
set /p CONFIRM="¿Deseas continuar cuando tengas la caja conectada por USB en Fastboot? (S/N): "
if /I not "%CONFIRM%"=="S" exit /b 0

set "SCRIPT_DIR=%~dp0"
cd /d "c:\Users\CORSAIR\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2"

echo [1/12] Detectando dispositivo en Fastboot...
fastboot devices
timeout /t 2 /nobreak >nul

echo [2/12] Desbloqueando bootloader (lock = 10100000 para aceptar vbmeta Flags=0x03)...
fastboot oem 'setenv lock 10100000;save
timeout /t 2 /nobreak >nul
fastboot reboot bootloader
timeout /t 4 /nobreak >nul
fastboot getvar unlocked

echo [3/12] Flasheando boot.img (100%% Oficial ZTE verificado)...
fastboot flash boot boot.img

echo [4/12] Flasheando _aml_dtb y dtbo.img (100%% Oficial ZTE verificado)...
fastboot flash _aml_dtb dtb.img
fastboot flash dtbo dtbo.img

echo [5/12] OMITIENDO factory.img y cri_data.img para conservar tu Widevine L1 y MAC originales...

echo [6/12] Flasheando logo.img y param.img...
fastboot flash logo logo.img
fastboot flash param param.img

echo [7/12] Flasheando conf.img y limpiando oem.img...
fastboot flash conf conf.img
fastboot flash oem oem.img

echo [8/12] Flasheando super.img (Android 12 liberado)...
fastboot flash super super.img

echo [9/12] Flasheando vbmeta.img (Flags=0x03 dm-verity desactivado)...
fastboot flash vbmeta vbmeta.img

echo [10/12] Flasheando recovery.img (100%% Oficial ZTE verificado)...
fastboot flash recovery recovery.img

echo [11/12] Reparando particion /cache con superbloque ext4 valido (cache_ext4.img)...
fastboot erase cache
fastboot flash cache "%SCRIPT_DIR%cache_ext4.img"

echo [12/12] Limpiando userdata y reiniciando...
fastboot erase userdata
fastboot reboot

echo.
echo [COMPLETADO] Tu caja Xview B866V2 se reiniciara en Android 12 con /cache reparada.
pause
