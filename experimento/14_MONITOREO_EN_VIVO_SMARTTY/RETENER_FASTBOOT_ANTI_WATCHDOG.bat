@echo off
chcp 65001 >nul
title Retenedor Anti-Watchdog Fastboot - ZTE B866V2 (U-Boot 2015.01-g220e992)
color 0A

set "ROM_DIR=%~dp0..\..\V2\B866v2 Android 12\B866v2 Android 12\B866V2"
cd /d "%ROM_DIR%"

echo ================================================================================
echo   RETENEDOR ANTI-WATCHDOG PARA FASTBOOT (ZTE B866V2 - U-Boot 2015.01-g220e992)
echo ================================================================================
echo   Evita que el temporizador Watchdog (reboot_mode:::: watchdog_reboot) reinicie
echo   la placa por inactividad USB y cargue el sistema normal (ztebsp: bootcmd=bootsys).
echo.
echo   [PASO EN SMARTTY (Consola # de Recovery antes de reiniciar a Fastboot)]:
echo     echo -n "boot-recovery" ^> /dev/block/misc ; sync ; reboot bootloader
echo ================================================================================
echo.
echo [*] Esperando a que la placa entre en modo Fastboot...

:WAIT_LOOP
for /f "tokens=1,2" %%A in ('fastboot devices 2^>nul') do (
    if "%%B"=="fastboot" (
        echo [+] Placa detectada en Fastboot (%%A). Enviando desactivacion de Watchdog...
        fastboot oem "watchdog off" 2>nul
        goto :KEEP_ALIVE
    )
)
goto :WAIT_LOOP

:KEEP_ALIVE
echo [*] Mantenimiento Anti-Watchdog activo (pulso USB cada 2s). Presiona Ctrl+C para salir.
:PULSE_LOOP
fastboot getvar version >nul 2>&1
timeout /t 2 /nobreak >nul
goto :PULSE_LOOP
