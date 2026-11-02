@echo off
chcp 65001 >nul
title OCULTAR O DESACTIVAR DEPURACION POR USB PARA APPS - XVIEW+ (ZTE B866V2)
color 0B

set "BASE_DIR=%~dp0"
set "ADB=%BASE_DIR%FIRMWARE_COMPLETO_B866V2_FASTBOOT\adb.exe"
if not exist "%ADB%" set "ADB=%BASE_DIR%V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"
if not exist "%ADB%" set "ADB=%BASE_DIR%experimento\06_internet_y_repositorios\adb_tools\platform-tools\adb.exe"

echo ==============================================================================
echo   OCULTAR / DESACTIVAR DEPURACION POR USB Y OPCIONES DE DESARROLLADOR (XVIEW+)
echo   Evita que apps (Xview+, Megacable, streaming, bancos) bloqueen la pantalla
echo ==============================================================================
echo.
echo [1] Buscando caja Xview+ conectada por USB o Red...
"%ADB%" devices
echo.
set /p "IP_CAJA=Si conectas por Red Wi-Fi/Ethernet escribe su IP (ej. 192.168.0.47 o ENTER para USB): "
if not "%IP_CAJA%"=="" (
    echo [+] Conectando a %IP_CAJA%:5555...
    "%ADB%" connect %IP_CAJA%:5555
    set "ADB_TARGET=-s %IP_CAJA%:5555"
) else (
    set "ADB_TARGET="
)

echo.
echo ==============================================================================
echo   ELIGE EL MODO QUE DESEAS APLICAR:
echo ==============================================================================
echo   [1] MODO OCULTO / STEALTH (Recomendado)
echo       - Pone adb_enabled=0 y development_settings_enabled=0 en Settings
echo       - Las apps ven la Depuracion USB y Opciones de Desarrollador APAGADAS
echo       - Mantiene el demonio adbd activo en segundo plano (puerto 5555)
echo.
echo   [2] DESACTIVACION TOTAL (Cerrar ADB por completo)
echo       - Pone adb_enabled=0 y development_settings_enabled=0
echo       - Cambia el puerto USB a solo MTP (sin ADB) y apaga el servicio adbd
echo ==============================================================================
set /p "MODO=Selecciona 1 o 2 (ENTER = 1): "
if "%MODO%"=="" set "MODO=1"

echo.
echo [+] Ocultando banderas de Depuracion por USB y Opciones de Desarrollador en Android...
"%ADB%" %ADB_TARGET% shell "settings put global adb_enabled 0"
"%ADB%" %ADB_TARGET% shell "settings put secure adb_enabled 0"
"%ADB%" %ADB_TARGET% shell "settings put global development_settings_enabled 0"
"%ADB%" %ADB_TARGET% shell "settings put secure development_settings_enabled 0"
"%ADB%" %ADB_TARGET% shell "settings put global adb_wifi_enabled 0"

if "%MODO%"=="1" (
    echo [+] Configurando Modo Stealth ^(Settings reporta 0 a las apps, adbd sigue activo^)...
    "%ADB%" %ADB_TARGET% shell "setprop service.adb.tcp.port 5555; setprop ctl.start adbd" 2>nul
    echo.
    echo [Verificacion de valores visibles para las aplicaciones]:
    for /f "delims=" %%A in ('"%ADB%" %ADB_TARGET% shell "settings get global adb_enabled" 2^>nul') do echo   - settings global adb_enabled                  = %%A
    for /f "delims=" %%A in ('"%ADB%" %ADB_TARGET% shell "settings get global development_settings_enabled" 2^>nul') do echo   - settings global development_settings_enabled = %%A
) else (
    echo [+] Aplicando Desactivacion Total de ADB...
    "%ADB%" %ADB_TARGET% shell "setprop persist.service.adb.enable 0; setprop persist.sys.usb.config mtp; setprop sys.usb.config mtp; stop adbd" 2>nul
)

echo.
echo ==============================================================================
echo   [OK] Listo. Las aplicaciones ya no detectaran la Depuracion por USB activa.
echo   (Si la app estaba abierta, cierrala o fuerza detencion y vuelvela a abrir).
echo ==============================================================================
pause
