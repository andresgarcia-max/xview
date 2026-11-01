@echo off
chcp 65001 >nul
title LIBERAR XVIEW+ (ZTE B866V2) SIN ABRIR LA CAJA (PORTATIL DESDE USB O PC)
color 0B

set "BASE_DIR=%~dp0"
if exist "%~dp0xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe" set "BASE_DIR=%~dp0xview\"
if not exist "%BASE_DIR%V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe" set "BASE_DIR=C:\Users\CORSAIR\Desktop\xview\"

set "ADB=%BASE_DIR%V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"
set "FASTBOOT=%BASE_DIR%V2\B866v2 Android 12\B866v2 Android 12\B866V2\fastboot.exe"
set "APK_AUTOSTART=%BASE_DIR%experimento\09_analisis_archivos_usuario\autostart.apk"
set "FLASH_SEGURO=%BASE_DIR%experimento\11_VALIDACION_OTA_Y_FIRMWARE_ZTE\FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat"

echo ============================================================================
echo   KIT DE LIBERACION PORTATIL SIN ABRIR LA CAJA - XVIEW+ (ZTE B866V2)
echo ============================================================================
echo.
echo [1] Buscando caja Xview por cable USB...
"%ADB%" devices
echo.
set /p "IP_CAJA=Si la conectas por Red Wi-Fi/Ethernet escribe su IP (ej. 192.168.0.47 o ENTER para USB): "
if not "%IP_CAJA%"=="" (
    echo [+] Conectando a %IP_CAJA%:5555...
    "%ADB%" connect %IP_CAJA%:5555
    set "ADB_TARGET=-s %IP_CAJA%:5555"
) else (
    set "ADB_TARGET="
)

echo.
echo ============================================================================
echo   SELECCIONA QUE DESEAS HACER SIN ABRIR LA CAJA:
echo ============================================================================
echo   [1] Liberar completa por ADB (Quitar Megacable + Deshabilitar telemetria/OTA ZTE
echo       + Quitar banner "No se pudo instalar la actualizacion del sistema")
echo   [2] Meter la caja cerrada en modo FASTBOOT y flashear Android 12 Completo
echo       (conservando Widevine L1 original y reparando /cache)
echo   [3] Solo deshabilitar paquetes de actualizacion/telemetria ZTE y quitar banner
echo ============================================================================
set /p "OPCION=Elige 1, 2 o 3: "

if "%OPCION%"=="2" (
    echo.
    echo [+] Enviando comando 'adb reboot bootloader' y activando enganche inmediato Anti-Watchdog...
    "%ADB%" %ADB_TARGET% reboot bootloader
    call "%FLASH_SEGURO%" --auto
    exit /b 0
)

if "%OPCION%"=="1" (
    echo.
    echo [+] Instalando aplicacion de inicio y liberando restricciones de operador...
    if exist "%APK_AUTOSTART%" "%ADB%" %ADB_TARGET% install -r "%APK_AUTOSTART%"
    "%ADB%" %ADB_TARGET% shell pm disable-user --user 0 com.dotscreen.megacable.atv 2>nul
    "%ADB%" %ADB_TARGET% shell pm disable-user --user 0 com.ztestb.LauncherCustomize 2>nul
    "%ADB%" %ADB_TARGET% shell pm disable-user --user 0 com.zte.ums.oes 2>nul
    "%ADB%" %ADB_TARGET% shell pm disable-user --user 0 com.zte.ota 2>nul
)

echo.
echo [+] Deshabilitando paquetes residuales de actualizacion/telemetria ZTE...
for %%P in (
    tv.upgrade.service
    com.zte.tv.act
    com.ztestb.uploaddiagno
    com.zte.factorytesttool
) do (
    "%ADB%" %ADB_TARGET% shell "am force-stop %%P; pm disable-user --user 0 %%P" 2>nul
)

echo [+] Eliminando banner "No se pudo instalar la actualizacion del sistema"...
"%ADB%" %ADB_TARGET% shell "settings put global ota_disable_automatic_update 1" 2>nul
"%ADB%" %ADB_TARGET% shell "cmd notification snooze --for 315360000000 '0|com.google.android.gms|1|com.google.android.gms.update.control.NotificationControl|10049'" 2>nul
"%ADB%" %ADB_TARGET% shell "cmd appops set com.google.android.gms POST_NOTIFICATION ignore" 2>nul
"%ADB%" %ADB_TARGET% shell "am force-stop com.google.android.gms" 2>nul
"%ADB%" %ADB_TARGET% shell settings put global adb_enabled 1
"%ADB%" %ADB_TARGET% shell setprop persist.service.adb.enable 1
"%ADB%" %ADB_TARGET% shell setprop service.adb.tcp.port 5555

echo.
echo ============================================================================
echo   [OK] Proceso completado exitosamente.
echo ============================================================================
pause
