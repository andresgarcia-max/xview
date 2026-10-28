@echo off
chcp 65001 >nul
title DESBLOQUEO DE PARTICION CONF Y BOOTLOADER ZTE (B820C-A15 LE / B866V2 / B860H)
echo ============================================================================
echo   DESBLOQUEO DE OPCIONES DE DESARROLLADOR (PARTICION CONF) Y BOOTLOADER
echo   Metodo ZTE S905X2: Respalda 'conf' original y flashea 'conf.PARTITION' limpia
echo   SIN tocar el bootloader.img (seguro para DDR3 del B820C-A15 y DDR4 del B866V2)
echo ============================================================================
echo.

setlocal EnableDelayedExpansion
set "UPDATER=%~dp0..\..\02_repositorios_github_y_herramientas\tohenk__b860hv5-os10-root\bin\aml-flash-tool\update.exe"
set "CLEAN_CONF=%~dp0..\..\02_repositorios_github_y_herramientas\tohenk__b860hv5-os10-root\data\conf.PARTITION"

if not exist "%UPDATER%" (
    echo [ERROR] No se encontro update.exe.
    pause
    exit /b 1
)

echo [1] Verificando conexion en modo Amlogic WorldCup USB...
"%UPDATER%" scan > "%TEMP%\aml_scan.txt" 2>&1
findstr /i "port" "%TEMP%\aml_scan.txt" >nul
if not %ERRORLEVEL%==0 (
    echo [!] Dispositivo no detectado en modo WorldCup USB. Conecta el cable USB Macho-a-Macho.
    pause
    exit /b 1
)

echo [2] Guardando respaldo de seguridad de la particion 'conf' original (4 MB)...
"%UPDATER%" mread store conf normal 0x400000 "%~dp0conf_original_backup.img"
if not exist "%~dp0conf_original_backup.img" (
    echo [ERROR] No se pudo respaldar la particion conf original. Abortando por seguridad.
    pause
    exit /b 1
)

echo [3] Desbloqueando bandera de U-Boot (setenv -f lock 10100000)...
"%UPDATER%" bulkcmd "setenv -f lock 10100000"
"%UPDATER%" bulkcmd "setenv otg_device 2"
"%UPDATER%" bulkcmd "saveenv"

echo [4] Flasheando particion 'conf' desbloqueada (habilita Opciones de Desarrollador y ADB)...
"%UPDATER%" partition conf "%CLEAN_CONF%"

echo.
echo ============================================================================
echo [OK] Proceso terminado.
echo   1. Reinicia el decodificador y conectalo a tu red Wi-Fi o Ethernet.
echo   2. Ve a Ajustes -> Preferencias del dispositivo -> Informacion -> Compilacion
echo      y presiona 7 veces OK (ahora SI permitira activar Opciones de Desarrollador).
echo   3. Activa Depuracion USB y ejecuta 3_OPTIMIZAR_Y_LIBERAR_LAUNCHER_ADB.bat.
echo ============================================================================
pause
