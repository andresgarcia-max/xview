@echo off
setlocal ENABLEDELAYEDEXPANSION
title ZTE-B866V2-OS12 [EVOCA SIN TTL - SALTO AUTOMATICO FASTBOOTD -> FASTBOOT]
chcp 65001 >nul
cls
cd /d "%~dp0B866V2"

echo ============================================================================
echo   ZTE B866V2 ANDROID 12 - EVOCA (100%% EXTERNO SIN TTL / ANTI-WATCHDOG)
echo ============================================================================
echo [*] Si tu TV ya esta en la pantalla "fastbootd":
echo     -> Selecciona "Reboot to bootloader" en la TV (con control o teclado USB),
echo     -> O desconecta la corriente estando en "fastbootd", manten RESET y conecta.
echo        (Como fastbootd ya activo otg_device=1, encendera el USB sin usar TTL).
echo [*] Esperando conexion USB de la caja (fastbootd o fastboot)...

:WAIT_FASTBOOT
fastboot devices | findstr /i "fastboot" >nul
if errorlevel 1 (
    goto WAIT_FASTBOOT
)

REM Verificar si la caja entro en fastbootd (PID_4EE7 / is-userspace: yes) para hacer el salto sin TTL
fastboot getvar is-userspace 2>&1 | findstr /i "yes" >nul
if not errorlevel 1 (
    echo [+] Pantalla fastbootd detectada por USB! Ejecutando salto automatico a Bootloader ^(sin TTL^)...
    fastboot reboot bootloader
    timeout /t 2 /nobreak >nul
    goto WAIT_FASTBOOT
)

echo [+] Caja conectada en U-Boot Fastboot! Ejecutando secuencia EVOCA...
fastboot oem "watchdog off"
fastboot oem 'setenv lock 10100000;save
fastboot oem "setenv lock 10100000;save"
fastboot oem "setenv otg_device 1;save"
fastboot flashing unlock_critical
fastboot flashing unlock
fastboot oem "saveenv"
fastboot reboot bootloader

:WAIT_REBOOT_BL
fastboot devices | findstr /i "fastboot" >nul
if errorlevel 1 (
    goto WAIT_REBOOT_BL
)

fastboot oem "watchdog off"
fastboot oem 'setenv lock 10100000;save
fastboot getvar unlocked

echo [*] Inyectando Firmware Android 12 Liberado ^(protegiendo Widevine L1 y MAC/SN^)...
fastboot flash boot boot.img
fastboot flash bootloader bootloader.img
fastboot flash _aml_dtb dtb.img
fastboot flash dtbo dtbo.img
REM SE OMITE factory.img y cri_data.img PARA NO BORRAR CLAVES DRM WIDEVINE L1 NI MAC/SN
fastboot flash logo logo.img
fastboot flash param param.img
fastboot flash conf conf.img
fastboot flash oem oem.img
fastboot flash super super.img
fastboot flash vbmeta vbmeta.img
fastboot flash recovery recovery.img
if exist "..\..\..\experimento\11_VALIDACION_OTA_Y_FIRMWARE_ZTE\cache_ext4.img" (
    fastboot flash cache "..\..\..\experimento\11_VALIDACION_OTA_Y_FIRMWARE_ZTE\cache_ext4.img"
) else (
    fastboot erase cache
)
fastboot erase userdata

echo ============================================================================
echo [+] ¡EXITO TOTAL! RETENIENDO CAJA EN FASTBOOT ^(ANTI-WATCHDOG ACTIVO^)
echo ============================================================================
:KEEP_ALIVE
fastboot getvar version >nul 2>&1
timeout /t 2 /nobreak >nul
goto KEEP_ALIVE
