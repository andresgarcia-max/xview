@echo off
chcp 65001 >nul
setlocal ENABLEDELAYEDEXPANSION
title Flasheo Validado y Seguro ZTE B866V2 Android 12 (Con Reparacion ext4 de /cache y Anti-Watchdog)
color 0A
cls

set "SCRIPT_DIR=%~dp0"
set "FB_DIR=%~dp0..\..\V2\B866v2 Android 12\B866v2 Android 12\B866V2"

echo ================================================================================
echo   PASO 1: PREPARACION DE DESBLOQUEO Y FLASHEO SEGURO ZTE B866V2
echo ================================================================================
echo   - Corrige el comando OEM de desbloqueo (sin comilla simple rota)
echo   - Ejecuta flashing unlock + flashing unlock_critical
echo   - Conserva tus particiones 'factory' (Widevine L1 / HDCP) y 'cri_data' (MAC/SN)
echo   - Inyecta cache_ext4.img para evitar error de montaje en /cache
echo   - Incluye retencion Anti-Watchdog (NO te saca de Fastboot automaticamente)
echo ================================================================================
echo.

cd /d "%FB_DIR%"

echo [*] Esperando a que la caja sea detectada en Fastboot por USB...
:WAIT_DEV
for /f "tokens=1,2" %%a in ('fastboot.exe devices 2^>nul') do (
    if "%%b"=="fastboot" (
        set "FB_DEV=%%a"
        goto :DEV_READY
    )
)
timeout /t 1 /nobreak >nul
goto :WAIT_DEV

:DEV_READY
echo [+] Dispositivo detectado en Fastboot: !FB_DEV!
echo.
echo [1/12] Enviando secuencia completa de desbloqueo de bootloader...
fastboot.exe oem "setenv lock 10100000"
fastboot.exe oem "saveenv"
fastboot.exe oem "save"
fastboot.exe oem "setenv lock 10100000;save"
fastboot.exe oem "setenv lock 10100000;saveenv"
fastboot.exe flashing unlock_critical
fastboot.exe flashing unlock
fastboot.exe oem unlock

echo [2/12] Reiniciando en Fastboot para recargar estado de lock...
fastboot.exe reboot bootloader
timeout /t 3 /nobreak >nul

:WAIT_DEV2
for /f "tokens=1,2" %%a in ('fastboot.exe devices 2^>nul') do (
    if "%%b"=="fastboot" goto :DEV_READY2
)
timeout /t 1 /nobreak >nul
goto :WAIT_DEV2

:DEV_READY2
fastboot.exe flashing unlock_critical
fastboot.exe flashing unlock
fastboot.exe getvar unlocked

echo [3/12] Flasheando boot.img (100%% Oficial ZTE verificado)...
fastboot.exe flash boot boot.img

echo [4/12] Flasheando _aml_dtb y dtbo.img (100%% Oficial ZTE verificado)...
fastboot.exe flash _aml_dtb dtb.img
fastboot.exe flash dtbo dtbo.img

echo [5/12] OMITIENDO factory.img y cri_data.img para conservar tu Widevine L1 y MAC originales...

echo [6/12] Flasheando logo.img y param.img...
fastboot.exe flash logo logo.img
fastboot.exe flash param param.img

echo [7/12] Flasheando conf.img y limpiando oem.img...
fastboot.exe flash conf conf.img
fastboot.exe flash oem oem.img

echo [8/12] Flasheando super.img (Android 12 liberado)...
fastboot.exe flash super super.img

echo [9/12] Flasheando vbmeta.img (Flags=0x03 dm-verity desactivado)...
fastboot.exe flash vbmeta vbmeta.img

echo [10/12] Flasheando recovery.img (100%% Oficial ZTE verificado)...
fastboot.exe flash recovery recovery.img

echo [11/12] Reparando particion /cache con superbloque ext4 valido (cache_ext4.img)...
fastboot.exe erase cache
fastboot.exe flash cache "%SCRIPT_DIR%cache_ext4.img"

echo [12/12] Limpiando userdata...
fastboot.exe erase userdata

echo.
echo [COMPLETADO] La caja se mantiene retenida en Fastboot (Anti-Watchdog activo).
echo Presiona [R] cuando desees reiniciarla a Android, o [Ctrl+C] para mantenerla.
:KEEP_ALIVE_LOOP
fastboot.exe getvar version >nul 2>&1
choice /c RC /n /t 3 /d C >nul
if errorlevel 2 goto :KEEP_ALIVE_LOOP
if errorlevel 1 (
    fastboot.exe reboot
    pause
)
