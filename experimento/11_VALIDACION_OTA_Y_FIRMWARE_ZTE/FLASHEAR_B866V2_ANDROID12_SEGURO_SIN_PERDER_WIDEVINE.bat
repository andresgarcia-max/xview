@echo off
chcp 65001 >nul
title Flasheo Validado y Seguro ZTE B866V2 Android 12 (USB Macho-a-Macho Sin TTL)
cls

if /I "%~1"=="--auto" goto SKIP_VALIDATION

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
echo   PASO 2: FLASHEO 100%% USB MACHO-A-MACHO (SIN TTL) + OTG + RECOVERY TV
echo ================================================================================
echo   - Conserva tus particiones 'factory' (Widevine L1 / HDCP) y 'cri_data' (MAC).
echo   - Si la caja esta en Android con Depuracion USB (ADB), la envia sola a Fastboot.
echo   - Desactiva el Hardware Watchdog (~14s) al instante de detectar Fastboot.
echo   - Habilita permanentemente el puerto USB OTG (setenv otg_device 1;save).
echo   - Instala recovery.img con menu completo desbloqueado en TV (Fastboot / ADB).
echo   - Inyecta cache_ext4.img formateada con mke2fs para reparar /cache.
echo ================================================================================
echo.
set /p CONFIRM="¿Deseas iniciar la espera activa para atrapar y flashear la caja por USB? (S/N): "
if /I not "%CONFIRM%"=="S" exit /b 0

:SKIP_VALIDATION
set "SCRIPT_DIR=%~dp0"
cd /d "%~dp0..\..\V2\B866v2 Android 12\B866v2 Android 12\B866V2"

echo.
echo [0/12] Verificando si la caja ya esta conectada en Android (ADB)...
adb devices | findstr /r /c:"device$" >nul 2>&1
if not errorlevel 1 (
    echo        [+] Caja detectada en ADB! Enviando 'adb reboot bootloader' automaticamente...
    adb reboot bootloader
)

echo [1/12] Esperando activamente en el puerto USB 2.0 a que la caja entre en Fastboot...
:WAIT_FASTBOOT
fastboot devices | findstr /r /c:"fastboot" >nul 2>&1
if not errorlevel 1 goto FASTBOOT_CAUGHT
adb devices | findstr /r /c:"device$" >nul 2>&1
if not errorlevel 1 (
    echo        [+] Caja detectada en ADB! Enviando 'adb reboot bootloader' automaticamente...
    adb reboot bootloader
)
goto WAIT_FASTBOOT

:FASTBOOT_CAUGHT
echo [2/12] Dispositivo detectado! Apagando Watchdog, desbloqueando Bootloader y activando USB OTG...
fastboot oem "watchdog off"
fastboot flashing unlock_critical
fastboot flashing unlock
fastboot oem "setenv lock 10100000;save"
fastboot oem "setenv otg_device 1;save"
fastboot oem "setenv lock 10100000"
fastboot oem "setenv otg_device 1"
fastboot oem "saveenv"
fastboot reboot bootloader

echo        Esperando reconexion del bootloader...
:WAIT_FASTBOOT_2
fastboot devices | findstr /r /c:"fastboot" >nul 2>&1
if errorlevel 1 goto WAIT_FASTBOOT_2
fastboot oem "watchdog off"
fastboot flashing unlock_critical
fastboot flashing unlock
fastboot oem "setenv lock 10100000;save"
fastboot oem "setenv otg_device 1;save"
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

echo [10/12] Flasheando recovery.img (Menu Fastboot/ADB desbloqueado en TV para MarketCode 4301)...
fastboot flash recovery recovery.img

echo [11/12] Reparando particion /cache con superbloque ext4 valido (cache_ext4.img)...
fastboot erase cache
fastboot flash cache "%SCRIPT_DIR%cache_ext4.img"

echo [12/12] Limpiando userdata y reiniciando...
fastboot erase userdata
fastboot reboot

echo.
echo [COMPLETADO] Tu caja Xview B866V2 se reiniciara en Android 12 con USB OTG (otg_device=1),
echo              menu completo de Recovery en TV y particion /cache reparada.
pause
